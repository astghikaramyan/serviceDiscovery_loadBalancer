package com.example.songservice.controller;

import com.example.songservice.dto.SongDTO;
import com.example.songservice.entity.SongEntity;
import com.example.songservice.exception.InvalidDataException;
import com.example.songservice.exception.NotFoundException;
import com.example.songservice.mapper.SongMapper;
import com.example.songservice.model.ErrorResponse;
import com.example.songservice.model.SimpleErrorResponse;
import com.example.songservice.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/songs")
public class SongRestController {
    @Autowired
    private SongService songService;
    @Autowired
    private SongMapper songMapper;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> addSongMetadata(@RequestBody @Valid SongDTO songDTO) {
        try {
            ErrorResponse errorResponse = songService.checkValidity(songDTO);
            if (Objects.nonNull(errorResponse) && Objects.nonNull(errorResponse.getErrorMessage()) && !errorResponse.getErrorMessage().isEmpty()) {
                throw new InvalidDataException(errorResponse);
            }
            SimpleErrorResponse simpleErrorResponse = songService.checkMissingFields(songDTO);
            if (Objects.nonNull(simpleErrorResponse) && Objects.nonNull(simpleErrorResponse.getErrorMessage()) && !simpleErrorResponse.getErrorMessage().isEmpty()) {
                throw new InvalidDataException(simpleErrorResponse);
            }
            final SongEntity songEntity = songService.addSong(this.songMapper.mapToEntity(songDTO));
            return ResponseEntity.ok(prepareResponseObject(songEntity));
        } catch (final InvalidDataException e) {
            if(Objects.nonNull(e.getErrorResponse())){
                ErrorResponse e1 = e.getErrorResponse();
                throw new InvalidDataException(e1);
            }else {
                SimpleErrorResponse e2 = e.getSimpleErrorResponse();
                throw new InvalidDataException(e2);
            }
        } catch (final Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongMetadata(@PathVariable Integer id) {
        try {
            try {
                Integer.parseInt(String.valueOf(id));
                if (isNotValid(String.valueOf(id))) {
                    throw new InvalidDataException(String.format("Song ID=%s is not a positive whole number ", id));
                }
            } catch (final NumberFormatException e) {
                throw new InvalidDataException(prepareErrorResponse(String.format("%s must be a positive whole number", id), "400"));
            }
            final Optional<SongEntity> songEntity = songService.getSong(id);
            if (songEntity.isPresent()) {
                return ResponseEntity.ok(songMapper.mapToDTO(songEntity.get()));
            }
            throw new NotFoundException(String.format("Song metadata with id %d does not exist", id));
        } catch (NotFoundException e) {
            throw new NotFoundException(prepareErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.toString()));
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        } catch (Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteSongsMetadata(@RequestParam String id) {
        try {
            if (Objects.nonNull(id) && id.length() > 200) {
                throw new InvalidDataException("Characters length is higher than allowed. Max length is 200. ");
            }
            String[] ids = Optional.ofNullable(id).map(param -> param.split(",")).orElse(new String[]{});
            final Map<String, List<Integer>> responseObject = new HashMap<>();
            List<Integer> removedIds = new LinkedList<>();
            Arrays.stream(ids).forEach(param -> {
                try {
                    Integer.parseInt(param);
                    if (isNotValid(param)) {
                        throw new InvalidDataException(String.format("Song ID=%s is not a positive whole number ", param));
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidDataException(String.format("Provided param value %s is not a supported numeric one", param));
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
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        } catch (Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        }
    }

    @ExceptionHandler({NotFoundException.class})
    private ResponseEntity<Object> handleNotFoundException(final NotFoundException notFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Objects.nonNull(notFoundException.getErrorResponse()) ? notFoundException.getErrorResponse() : notFoundException.getSimpleErrorResponse());
    }

    @ExceptionHandler({NumberFormatException.class})
    private ResponseEntity<Object> handleNumberFormatException(final NumberFormatException numberFormatException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(numberFormatException.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    private ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException illegalArgumentException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(illegalArgumentException.getMessage());
    }

    @ExceptionHandler({InvalidDataException.class})
    private ResponseEntity<Object> handleInvalidDataException(final InvalidDataException invalidDataException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Objects.nonNull(invalidDataException.getErrorResponse()) ? invalidDataException.getErrorResponse() : invalidDataException.getSimpleErrorResponse());
    }

    private Map<String, Integer> prepareResponseObject(final SongEntity songEntity) {
        final Map<String, Integer> responseObject = new HashMap<>();
        responseObject.put("id", songEntity.getId());
        return responseObject;
    }

    private SimpleErrorResponse prepareErrorResponse(final String message, final String code) {
        final SimpleErrorResponse errorResponse = new SimpleErrorResponse();
        errorResponse.setErrorMessage(message);
        errorResponse.setErrorCode(code);
        return errorResponse;
    }

    private boolean isNotValid(String id){
        return Objects.isNull(id) || Integer.parseInt(id) <= 0;
    }
}
