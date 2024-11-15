package com.example.songservice.controller;

import com.example.songservice.dto.SongDTO;
import com.example.songservice.entity.SongEntity;
import com.example.songservice.exception.ConflictDataException;
import com.example.songservice.exception.InvalidDataException;
import com.example.songservice.exception.NotFoundException;
import com.example.songservice.mapper.SongMapper;
import com.example.songservice.model.ErrorResponse;
import com.example.songservice.model.ValidationErrorResponse;
import com.example.songservice.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/songs")
public class SongRestController {
    private static final String BAD_REQUEST_INCORRECT_NUMBER_ERROR_MESSAGE = "Invalid value %s for ID. Must be a positive integer";
    private static final String BAD_REQUEST_NOT_NUMBER_ERROR_MESSAGE = "Invalid ID format: %s for ID. Only positive integers are allowed";
    private static final String BAD_REQUEST_CSV_TOO_LONG_ERROR_MESSAGE = "CSV string is too long: received %s characters, maximum allowed is 200";
    private static final String BAD_REQUEST_RESPONSE_CODE = "400";
    private static final String NOT_FOUND_REQUEST_RESPONSE_CODE = "404";

    @Autowired
    private SongService songService;
    @Autowired
    private SongMapper songMapper;

    @PostMapping
    public ResponseEntity<Map<String, Integer>> addSongMetadata(@RequestBody @Valid SongDTO songDTO) {
        try {
            ValidationErrorResponse validationErrorResponse = songService.checkValidity(songDTO);
            if (Objects.nonNull(validationErrorResponse.getErrorMessage()) && !validationErrorResponse.getErrorMessage().isEmpty()) {
                throw new InvalidDataException(validationErrorResponse);
            }
            ErrorResponse errorResponse = songService.checkMissingFields(songDTO);
            if (Objects.nonNull(errorResponse.getErrorMessage()) && !errorResponse.getErrorMessage().isEmpty()) {
                throw new InvalidDataException(errorResponse);
            }
            if (Objects.nonNull(songDTO.getId()) && songService.existById(songDTO.getId())) {
                throw new ConflictDataException(String.format("Metadata for resource ID=%s already exists", songDTO.getId()));
            }
            final SongEntity songEntity = songService.addSong(this.songMapper.mapToEntity(songDTO));
            return ResponseEntity.ok(prepareResponseObject(songEntity));
        } catch (ConflictDataException e) {
            throw new ConflictDataException(prepareErrorResponse(e.getMessage(), "409"));
        } catch (final InvalidDataException e) {
            if (Objects.nonNull(e.getErrorResponse())) {
                ValidationErrorResponse e1 = e.getErrorResponse();
                throw new InvalidDataException(e1);
            } else {
                ErrorResponse e2 = e.getSimpleErrorResponse();
                throw new InvalidDataException(e2);
            }
        } catch (final Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDTO> getSongMetadata(@PathVariable Integer id) {
        try {
            if(!isNumeric(String.valueOf(id))){
                throw new InvalidDataException(String.format(BAD_REQUEST_NOT_NUMBER_ERROR_MESSAGE, id.toString()));
            }
            if (!isValidNumeric(String.valueOf(id))) {
                throw new InvalidDataException(String.format(BAD_REQUEST_INCORRECT_NUMBER_ERROR_MESSAGE, id.toString()));
            }
            if (songService.existById(id)) {
                final Optional<SongEntity> songEntity = songService.getSong(id);
                if (songEntity.isPresent()) {
                    return ResponseEntity.ok(songMapper.mapToDTO(songEntity.get()));
                }
            }
            throw new NotFoundException(String.format("Song metadata for ID=%d not found", id));
        } catch (NotFoundException e) {
            throw new NotFoundException(prepareErrorResponse(e.getMessage(), NOT_FOUND_REQUEST_RESPONSE_CODE));
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        } catch (Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        }
    }

    @GetMapping("/resource-identifiers/{resourceId}")
    public ResponseEntity<SongDTO> getSongMetadataByResourceId(@PathVariable Integer resourceId) {
        final Optional<SongEntity> songEntity = songService.getSongByResourceId(resourceId);

        if (songEntity.isPresent()) {
            return ResponseEntity.ok(songMapper.mapToDTO(songEntity.get()));
        }
        throw new NotFoundException(String.format("Song metadata for RESOURCE_ID=%d not found", resourceId));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteSongsMetadata(@RequestParam String id) {
        try {
            if (Objects.nonNull(id) && id.length() > 200) {
                throw new InvalidDataException(String.format(BAD_REQUEST_CSV_TOO_LONG_ERROR_MESSAGE, id.length()));
            }
            String[] ids = Optional.ofNullable(id).map(param -> param.split(",")).orElse(new String[]{});
            final Map<String, List<Integer>> responseObject = new HashMap<>();
            List<Integer> removedIds = new LinkedList<>();
            Arrays.stream(ids).forEach(param -> {
                if(!isNumeric(param)){
                    throw new InvalidDataException(String.format(BAD_REQUEST_NOT_NUMBER_ERROR_MESSAGE, param));
                }
                if (!isValidNumeric(param)) {
                    throw new InvalidDataException(String.format(BAD_REQUEST_INCORRECT_NUMBER_ERROR_MESSAGE, param));
                }
            });
            Arrays.stream(ids).forEach(param -> {
                Integer songIdentifier = Integer.valueOf(param);
                if (this.songService.existById(songIdentifier)) {
                    this.songService.deleteSong(songIdentifier);
                    removedIds.add(songIdentifier);
                }
            });
            responseObject.put("ids", removedIds);
            return ResponseEntity.ok(responseObject);
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        } catch (Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        }
    }

    private Map<String, Integer> prepareResponseObject(final SongEntity songEntity) {
        final Map<String, Integer> responseObject = new HashMap<>();
        responseObject.put("id", songEntity.getId());
        return responseObject;
    }

    private ErrorResponse prepareErrorResponse(final String message, final String code) {
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(message);
        errorResponse.setErrorCode(code);
        return errorResponse;
    }

    private boolean isValidNumeric(String id) {
        final boolean isWholeNumber = Optional.ofNullable(id)
                                              .map(s-> s.chars().allMatch(Character::isDigit))
                                              .orElse(false);
        return isWholeNumber && Integer.parseInt(id) > 0;
    }

    private boolean isNumeric(final String value) {
        return value!=null && value.matches("\\d+");
    }
}
