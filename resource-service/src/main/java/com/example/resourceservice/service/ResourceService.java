package com.example.resourceservice.service;

import com.example.resourceservice.dto.SongDTO;
import com.example.resourceservice.entity.ResourceEntity;
import com.example.resourceservice.exception.InvalidDataException;
import com.example.resourceservice.model.ErrorResponse;
import com.example.resourceservice.model.SongMetadata;
import com.example.resourceservice.repository.ResourceRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.UnsupportedMimeTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@Service
public class ResourceService {
    @Autowired
    private ResourceRepository repository;
    @Autowired
    private RestTemplate restTemplate;

    public ResourceEntity createResource(final byte[] audioData) {
        ResourceEntity resource = new ResourceEntity();
        resource.setData(audioData);
        resource = this.repository.save(resource);
        SongMetadata songMetadata = null;
        try {
            songMetadata = getFileMetadata(audioData);
        } catch (IOException e) {
            throw new InvalidDataException("Invalid file format: . Only MP3 files are allowed");
        } catch (TikaException e) {
            throw new InvalidDataException("Invalid file format: . Only MP3 files are allowed");
        } catch (SAXException e) {
            throw new InvalidDataException("Invalid file format: . Only MP3 files are allowed");
        }
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
        ResponseEntity<SongDTO> songDTO = getSongBasedOnResourceId(id);
        if (songDTO.getStatusCode().equals(HttpStatusCode.valueOf(200))) {
            deleteSong(songDTO.getBody().getId());
        }
        repository.deleteById(id);
    }



    public boolean existById(final Integer id){
        return this.repository.existsById(id);
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
            songMetadata.setDuration(resolveEmptyLength(formatDuration(metadata.get("xmpDM:duration"))));
            songMetadata.setYear(resolveEmptyYear(metadata.get("xmpDM:releaseDate")));
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
            return String.format("%d:%d", minutes, seconds);
        } catch (NumberFormatException e) {
            return "Unknown";
        }
    }

    private String resolveEmptyField(final String value) {
        return Optional.ofNullable(value).orElse("Unknown");
    }
    private String resolveEmptyLength(final String value) {
        if(Objects.nonNull(value) && value.contains(":")){
            String[] parts = value.split(":");
            if(parts.length == 3){
                return resolveDurationParts(parts[1]) + ":" + resolveDurationParts(parts[2]);
            } else if (parts.length == 2) {
                return resolveDurationParts(parts[0]) + ":" + resolveDurationParts(parts[1]);
            } else if(parts.length == 1){
                return "00:" + resolveDurationParts(parts[0]);
            }
        }
        return "00:22";
    }

    private String resolveDurationParts(final String part1) {
        if(part1.length() == 2){
            return part1;
        } else if (part1.length() == 1) {
            return "0"+part1;
        }else {
            return "00";
        }
    }

    private String resolveEmptyYear(final String value) {
        final boolean isCorrectYear = Optional.ofNullable(value)
                                              .filter(v-> v.length()==4)
                                              .map(s-> s.chars().allMatch(Character::isDigit))
                                              .orElse(false);
        return isCorrectYear ? value : "1987";
    }

    private ResponseEntity<String> createSongMetadata(final SongMetadata songMetadata) {
        String url = "http://song-service:8081/songs";
        return restTemplate.postForEntity(url, songMetadata, String.class);
    }
    private ResponseEntity<SongDTO> getSongBasedOnResourceId(final Integer id) {
        String url = "http://song-service:8081/songs/resource-identifiers/" + id;
        return restTemplate.getForEntity(url, SongDTO.class);
    }

    private void deleteSong(final Integer id) {
        String url = "http://song-service:8081/songs?id=" + id;
        restTemplate.delete(url);
    }

}

