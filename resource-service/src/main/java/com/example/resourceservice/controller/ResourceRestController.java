package com.example.resourceservice.controller;

import com.example.resourceservice.entity.ResourceEntity;
import com.example.resourceservice.exception.NotFoundException;
import com.example.resourceservice.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ResourceRestController {
    @Autowired
    private ResourceService resourceService;

    @PostMapping(value = "/resource-api/resources", consumes = "audio/mpeg")
    public ResponseEntity<Map<String, Integer>> uploadResource(@RequestBody byte[] audioData) {
        try {
            final ResourceEntity resourceEntity = resourceService.createResource(audioData);
            if (Objects.nonNull(resourceEntity)) {
                final Map<String, Integer> result = new HashMap<>();
                result.put("id", resourceEntity.getId());
                return ResponseEntity.ok(result);
            }
            throw new IllegalArgumentException("Validation failed, request body is invalid MP3");
        } catch (final Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/resource-api/resources/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Integer id) {
        try {
            final ResourceEntity resource = resourceService.getResource(id);
            return ResponseEntity.ok(resource.getData());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @DeleteMapping("/resource-api/resources")
    public ResponseEntity<Map<String, List<Integer>>> deleteResource(@RequestParam String id) {
        if (Objects.nonNull(id) && id.length() >= 200) {
            throw new IllegalArgumentException("Characters length is higher than allowed. Max length is 199. ");
        }
        String[] ids = Optional.ofNullable(id).map(param -> param.split(",")).orElse(new String[]{});
        try {
            List<Integer> removedIds = new LinkedList<>();
            Arrays.stream(ids).forEach(param -> {
                try {
                    Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(String.format("Provided param value %s is not a supported numeric one", param));
                }
            });
            Arrays.stream(ids).forEach(param -> {
                if (Objects.nonNull(this.resourceService.getResource(Integer.valueOf(param)))) {
                    Integer resourceId = Integer.valueOf(param);
                    this.resourceService.deleteResource(resourceId);
                    removedIds.add(resourceId);
                }
            });
            final Map<String, List<Integer>> responseObject = new HashMap<>();
            responseObject.put("ids", removedIds);
            return ResponseEntity.ok(responseObject);
        } catch (final NumberFormatException e) {
            throw new NumberFormatException();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(final NotFoundException notFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundException.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class})
    private ResponseEntity<Object> handleIllegalArgumentException(final IllegalArgumentException illegalArgumentException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(illegalArgumentException.getMessage());
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    private ResponseEntity<Object> handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(httpMediaTypeNotSupportedException.getMessage());
    }

    @ExceptionHandler({NumberFormatException.class})
    private ResponseEntity<Object> handleNumberFormatException(final NumberFormatException numberFormatException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(numberFormatException.getMessage());
    }
}
