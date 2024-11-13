package com.example.resourceservice.service;

import com.example.resourceservice.entity.ResourceEntity;
import com.example.resourceservice.model.SongMetadata;
import com.example.resourceservice.repository.ResourceRepository;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class ResourceService {
    @Autowired
    private ResourceRepository repository;
    @Autowired
    private RestTemplate restTemplate;

    public ResourceEntity createResource(final byte[] audioData) throws IOException, TikaException, SAXException {
        ResourceEntity resource = new ResourceEntity();
        resource.setData(audioData);
        resource = this.repository.save(resource);
        SongMetadata songMetadata = getFileMetadata(audioData);
        songMetadata.setResourceId(resource.getId());
        ResponseEntity<String> songMetadataResourceEntity = createSongMetadata(songMetadata);
        if (!songMetadataResourceEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            deleteResource(resource.getId());
            return null;
        }
        return resource;
    }

    public ResourceEntity getResource(final Integer id) {
        return this.repository.getById(id);
    }

    public void deleteResource(final Integer id) {
        repository.deleteById(id);
    }

    private SongMetadata getFileMetadata(final byte[] audioData) throws IOException, TikaException, SAXException {
        SongMetadata songMetadata = new SongMetadata();

        try (InputStream inputStream = new ByteArrayInputStream(audioData)) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Mp3Parser mp3Parser = new Mp3Parser();
            ParseContext parseContext = new ParseContext();

            mp3Parser.parse(inputStream, handler, metadata, parseContext);

            songMetadata.setName(resolveEmptyField(metadata.get("title")));
            songMetadata.setArtist(resolveEmptyField(metadata.get("xmpDM:artist")));
            songMetadata.setAlbum(resolveEmptyField(metadata.get("xmpDM:album")));
            songMetadata.setLength(resolveEmptyField(formatDuration(metadata.get("xmpDM:duration"))));
            songMetadata.setYear(resolveEmptyField(metadata.get("xmpDM:releaseDate")));
        }

        return songMetadata;
    }

    private String formatDuration(String durationMillis) {
        if (durationMillis == null) {
            return null;
        }
        try {
            double durationInSeconds = Double.parseDouble(durationMillis) / 1000;
            int minutes = (int) (durationInSeconds / 60);
            int seconds = (int) (durationInSeconds % 60);
            return String.format("%d:%02d", minutes, seconds);
        } catch (NumberFormatException e) {
            return "Unknown";
        }
    }

    private String resolveEmptyField(final String value) {
        return Optional.ofNullable(value).orElse("Unknown");
    }

    private ResponseEntity<String> createSongMetadata(final SongMetadata songMetadata) {
        String url = "http://song-service:8081/songs";
        return restTemplate.postForEntity(url, songMetadata, String.class);
    }
}

