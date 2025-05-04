# MusicManager

## Table of Contents
 - [MavenSetup](#Maven)
 - [Examples](#Examples)
 

## Maven
       
    <repositories>
        <!-- Custom Repository -->
        <repository>
            <id>xyz.zeyso</id>
            <url>https://repo.zeyso.xyz/repo/</url>
        </repository>

    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.googlecode.soundlibs</groupId>
            <artifactId>mp3spi</artifactId>
            <version>1.9.5-1</version>
        </dependency>
        <dependency>
            <groupId>com.googlecode.soundlibs</groupId>
            <artifactId>tritonus-share</artifactId>
            <version>0.3.7-2</version>
        </dependency>
        <!-- Custom Dependency -->
        <dependency>
            <groupId>xyz.zeyso</groupId>
            <artifactId>musicmanager</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

## Examples
            MusicManager musicManager = new MusicManager();
            String streamUrl = "http://streams.ilovemusic.de/iloveradio2.mp3";
            musicManager.setVolume(0.1f);
            musicManager.playMp3Stream(streamUrl);
