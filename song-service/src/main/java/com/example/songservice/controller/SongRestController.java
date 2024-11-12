package com.example.songservice.controller;

import com.example.songservice.dto.SongDTO;
import com.example.songservice.entity.SongEntity;
import com.example.songservice.exception.NotFoundException;
import com.example.songservice.mapper.SongMapper;
import com.example.songservice.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
public class SongRestController {
    @Autowired
    private SongService songService;
    @Autowired
    private SongMapper songMapper;

    @PostMapping("/api/songs")
    public ResponseEntity<Map<String, Integer>> addSongMetadata(@RequestBody @Valid SongDTO songDTO) {
        try {
            final SongEntity songEntity = songService.addSong(this.songMapper.mapToEntity(songDTO));
            return ResponseEntity.ok(prepareResponseObject(songEntity));
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (final Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/songs/{id}")
    public ResponseEntity<SongDTO> getSongMetadata(@PathVariable Integer id) {
        try {
            final Optional<SongEntity> songEntity = songService.getSong(id);
            if (songEntity.isPresent()) {
                return ResponseEntity.ok(songMapper.mapToDTO(songEntity.get()));
            }
            throw new NotFoundException(String.format("Song metadata with id %d does not exist", id));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/api/songs")
    public ResponseEntity<Map<String, List<Integer>>> deleteSongsMetadata(@RequestParam String id) {
        if (Objects.nonNull(id) && id.length() >= 200) {
            throw new IllegalArgumentException("Characters length is higher than allowed. Max length is 199. ");
        }
        String[] ids = Optional.ofNullable(id).map(param -> param.split(",")).orElse(new String[]{});
        try {
            final Map<String, List<Integer>> responseObject = new HashMap<>();
            List<Integer> removedIds = new LinkedList<>();
            Arrays.stream(ids).forEach(param -> {
                try {
                    Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(String.format("Provided param value %s is not a supported numeric one", param));
                }
            });
            Arrays.stream(ids).forEach(param -> {
                if (this.songService.getSong(Integer.valueOf(param)).isPresent()) {
                    Integer songId = Integer.valueOf(param);
                    this.songService.deleteSong(songId);
                    removedIds.add(songId);
                }
            });
            responseObject.put("ids", removedIds);
            return ResponseEntity.ok(responseObject);
        }catch (final NumberFormatException e){
            throw new NumberFormatException();
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(final NotFoundException notFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundException.getMessage());
    }

    @ExceptionHandler({NumberFormatException.class})
    private ResponseEntity<Object> handleNumberFormatException(final NumberFormatException numberFormatException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(numberFormatException.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    private ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException illegalArgumentException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(illegalArgumentException.getMessage());
    }

    private Map<String, Integer> prepareResponseObject(final SongEntity songEntity) {
        final Map<String, Integer> responseObject = new HashMap<>();
        responseObject.put("id", songEntity.getId());
        return responseObject;
    }
}
