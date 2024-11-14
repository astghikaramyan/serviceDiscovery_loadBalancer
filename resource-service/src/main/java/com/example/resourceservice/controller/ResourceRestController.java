package com.example.resourceservice.controller;

import com.example.resourceservice.entity.ResourceEntity;
import com.example.resourceservice.exception.InvalidDataException;
import com.example.resourceservice.exception.NotFoundException;
import com.example.resourceservice.model.SimpleErrorResponse;
import com.example.resourceservice.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/resources")
public class ResourceRestController {
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
            throw new InvalidDataException(prepareErrorResponse("Validation failed", HttpStatus.BAD_REQUEST.toString()));
        } catch (Exception e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        }
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getResource(@PathVariable Integer id) {
        try {
            try {
                Integer.parseInt(String.valueOf(id));
                if (isNotValid(String.valueOf(id))) {
                    throw new InvalidDataException(String.format("Resource ID=%s is not a positive whole number ", id));
                }
            } catch (final NumberFormatException e) {
                throw new InvalidDataException(String.format("Resource ID=%s is not a positive whole number ", id));
            }
            final ResourceEntity resource = resourceService.getResource(id);
            if (Objects.nonNull(resource) && Objects.nonNull(resource.getData())) {
                return ResponseEntity.ok(resource.getData());
            }
            throw new NotFoundException(String.format("Resource with ID=%s not found", id));
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        } catch (NotFoundException e) {
            throw new NotFoundException(prepareErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.toString()));
        } catch (final Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Integer>>> deleteResource(@RequestParam String id) {
        try {
            if (Objects.nonNull(id) && id.length() > 200) {
                throw new InvalidDataException("Characters length is higher than allowed. Max length is 200. ");
            }
            String[] ids = Optional.ofNullable(id).map(param -> param.split(",")).orElse(new String[]{});
            Arrays.stream(ids).forEach(param -> {
                try {
                    Integer.parseInt(param);
                    if (isNotValid(param)) {
                        throw new InvalidDataException(String.format("Provided param value %s is not a whole positive number", param));
                    }
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException(String.format("Provided param value %s is not a supported numeric one", param));
                }
            });
            List<Integer> removedIds = new LinkedList<>();
            Arrays.stream(ids).forEach(param -> {
                if (Objects.nonNull(this.resourceService.getResource(Integer.valueOf(param))) && this.resourceService.getResource(Integer.valueOf(param)).getId().equals(Integer.valueOf(param))) {
                    Integer resourceId = Integer.valueOf(param);
                    this.resourceService.deleteResource(resourceId);
                    removedIds.add(resourceId);
                }
            });
            final Map<String, List<Integer>> responseObject = new HashMap<>();
            responseObject.put("ids", removedIds);
            return ResponseEntity.ok(responseObject);
        } catch (InvalidDataException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        } catch (final NumberFormatException e) {
            throw new InvalidDataException(prepareErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.toString()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @ExceptionHandler({InvalidDataException.class})
    private ResponseEntity<Object> handleInvalidDataException(final InvalidDataException invalidDataException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Objects.nonNull(invalidDataException.getErrorResponse()) ? invalidDataException.getErrorResponse() : invalidDataException.getSimpleErrorResponse());
    }

    @ExceptionHandler({NumberFormatException.class})
    private ResponseEntity<Object> handleNumberFormatException(final NumberFormatException numberFormatException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(numberFormatException.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    private ResponseEntity<Object> handleNotFoundException(final NotFoundException notFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Objects.nonNull(notFoundException.getErrorResponse()) ? notFoundException.getErrorResponse() : notFoundException.getSimpleErrorResponse());
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    private ResponseEntity<Object> handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(prepareErrorResponse("Invalid media type", "400"));
    }

    private boolean isNotValid(String id){
        return Objects.isNull(id) || Integer.parseInt(id) <= 0;
    }

    private SimpleErrorResponse prepareErrorResponse(final String message, final String code) {
        final SimpleErrorResponse errorResponse = new SimpleErrorResponse();
        errorResponse.setErrorMessage(message);
        errorResponse.setErrorCode(code);
        return errorResponse;
    }

}
