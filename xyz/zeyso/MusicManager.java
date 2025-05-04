package xyz.zeyso;
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class MusicManager {

    private SourceDataLine line;
    private final AtomicReference<Float> volume = new AtomicReference<>(0.5f); // Default volume set to 50%
    private Thread playbackThread;
    private volatile boolean isPlaying = false;

    public void playMp3Stream(String streamUrl) throws Exception {
        stop(); // Stop any currently playing stream

        isPlaying = true;
        playbackThread = new Thread(() -> {
            try (InputStream stream = new BufferedInputStream(new URL(streamUrl).openStream());
                 AudioInputStream mp3AudioStream = AudioSystem.getAudioInputStream(stream)) {

                // Convert MP3 to PCM format
                AudioFormat baseFormat = mp3AudioStream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );

                try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, mp3AudioStream)) {
                    playPcmStream(pcmStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        playbackThread.start();
    }

    private void playPcmStream(AudioInputStream pcmStream) throws Exception {
        try {
            AudioFormat format = pcmStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            // Buffer for playback
            byte[] buffer = new byte[4096];
            int bytesRead;

            while (isPlaying && (bytesRead = pcmStream.read(buffer, 0, buffer.length)) != -1) {
                float currentVolume = volume.get(); // Read volume dynamically
                for (int i = 0; i < bytesRead; i += 2) {
                    int sample = (buffer[i] & 0xFF) | (buffer[i + 1] << 8);
                    float scaledSample = sample * currentVolume;
                    scaledSample = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, scaledSample));
                    sample = (int) scaledSample;
                    buffer[i] = (byte) (sample & 0xFF);
                    buffer[i + 1] = (byte) ((sample >> 8) & 0xFF);
                }
                line.write(buffer, 0, bytesRead);
            }
        } finally {
            if (line != null) {
                line.drain();
                line.close();
            }
        }
    }

    public void setVolume(float volume) {
        this.volume.set(Math.max(0.0f, Math.min(1.0f, volume)));
        if (isPlaying) {
            try {
                playMp3Stream("http://streams.ilovemusic.de/iloveradio2.mp3"); // Restart with new volume
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        isPlaying = false; // Stop playback
        if (playbackThread != null) {
            try {
                playbackThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (line != null) {
            line.stop();
            line.close();
        }
    }

    public float getVolume() {
        return this.volume.get();
    }
}