package com.example.resourceservice.controller;

import com.example.resourceservice.entity.ResourceEntity;
import com.example.resourceservice.exception.InvalidDataException;
import com.example.resourceservice.exception.NotFoundException;
import com.example.resourceservice.model.ErrorResponse;
import com.example.resourceservice.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/resources")
public class ResourceRestController {
    private static final String BAD_REQUEST_INCORRECT_NUMBER_ERROR_MESSAGE = "Invalid value %s for ID. Must be a positive integer";
    private static final String BAD_REQUEST_NOT_NUMBER_ERROR_MESSAGE = "Invalid ID format: %s for ID. Only positive integers are allowed";
    private static final String BAD_REQUEST_CSV_TOO_LONG_ERROR_MESSAGE = "CSV string is too long: received %s characters, maximum allowed is 200";
    private static final String BAD_REQUEST_RESPONSE_CODE = "400";
    private static final String NOT_FOUND_REQUEST_RESPONSE_CODE = "404";


    @Autowired
    private ResourceService resourceService;

    @PostMapping(consumes = "audio/mpeg")
    public ResponseEntity<Map<String, Integer>> uploadResource(@RequestBody byte[] audioData) {
        try {
            final ResourceEntity resourceEntity = resourceService.createResource(audioData);
            if (Objects.nonNull(resourceEntity)) {
                final Map<String, Integer> result = new HashMap<>();
                result.put("id", resourceEntity.getId());
                return ResponseEntity.ok(result);
            }
            throw new InvalidDataException("Validation failed");
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        }
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getResource(@PathVariable Integer id) {
        try {

            if(!isNumeric(String.valueOf(id))){
                throw new InvalidDataException(String.format(BAD_REQUEST_NOT_NUMBER_ERROR_MESSAGE, id.toString()));
            }

            if (!isValidNumeric(String.valueOf(id))) {
                throw new InvalidDataException(String.format(BAD_REQUEST_INCORRECT_NUMBER_ERROR_MESSAGE,  id.toString()));
            }
            if (resourceService.existById(id)) {
                final ResourceEntity resource = resourceService.getResource(id);
                return ResponseEntity.ok(resource.getData());
            }
            throw new NotFoundException(String.format("Resource with ID=%s not found", id));
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        } catch (NotFoundException e) {
            throw new NotFoundException(prepareErrorResponse(e.getMessage(), NOT_FOUND_REQUEST_RESPONSE_CODE));
        } catch (final Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteResource(@RequestParam String id) {
        try {
            if (Objects.nonNull(id) && id.length() > 200) {
                throw new InvalidDataException(String.format(BAD_REQUEST_CSV_TOO_LONG_ERROR_MESSAGE, id.length()));
            }
            String[] ids = Optional.ofNullable(id).map(param -> param.split(",")).orElse(new String[]{});
            Arrays.stream(ids).forEach(param -> {
                if(!isNumeric(param)){
                    throw new InvalidDataException(String.format(BAD_REQUEST_NOT_NUMBER_ERROR_MESSAGE, param));
                }
                if (!isValidNumeric(param)) {
                    throw new InvalidDataException(String.format(BAD_REQUEST_INCORRECT_NUMBER_ERROR_MESSAGE, param));
                }
            });
            List<Integer> removedIds = new LinkedList<>();
            Arrays.stream(ids).forEach(param -> {
                Integer resourceId = Integer.valueOf(param);
                if (this.resourceService.existById(resourceId)) {
                    this.resourceService.deleteResource(resourceId);
                    removedIds.add(resourceId);
                }
            });
            final Map<String, List<Integer>> responseObject = new HashMap<>();
            responseObject.put("ids", removedIds);
            return ResponseEntity.ok(responseObject);
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), BAD_REQUEST_RESPONSE_CODE));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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

    private ErrorResponse prepareErrorResponse(final String message, final String code) {
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(message);
        errorResponse.setErrorCode(code);
        return errorResponse;
    }
}
