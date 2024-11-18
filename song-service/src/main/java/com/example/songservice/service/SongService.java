package com.example.songservice.service;

import com.example.songservice.dto.SongDTO;
import com.example.songservice.entity.SongEntity;
import com.example.songservice.model.ErrorResponse;
import com.example.songservice.model.ValidationErrorResponse;
import com.example.songservice.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private RestTemplate restTemplate;

    public SongEntity addSong(SongEntity songEntity) {
        return this.songRepository.save(songEntity);
    }

    public Optional<SongEntity> getSong(final Integer id) {
        return this.songRepository.findById(id);
    }
    public Optional<SongEntity> getSongByResourceId(final Integer resourceId) {
        return this.songRepository.findAll().stream().filter(songEntity -> songEntity.getResourceId().equals(resourceId)).findFirst();
    }


    public void deleteSong(final Integer id) {
        this.songRepository.deleteById(id);
    }
    public boolean existById(final Integer id){
        return this.songRepository.existsById(id);
    }
    public ValidationErrorResponse checkValidity(SongDTO songDTO){
        final ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse();
        final Map<String, String> errorDetails = new HashMap<>();
        if(!isValidYear(songDTO.getYear())){
            validationErrorResponse.setErrorCode("400");
            validationErrorResponse.setErrorMessage("Validation error");
        }
        if(!isValidDuration(songDTO.getDuration())){
            validationErrorResponse.setErrorCode("400");
            validationErrorResponse.setErrorMessage("Validation error");
        }
        errorDetails.put("duration", "Duration must be in mm:ss format");
        errorDetails.put("year", "year must be between 1900 and 2099");
        validationErrorResponse.setErrorDetails(errorDetails);
        return validationErrorResponse;
    }

    public ErrorResponse checkMissingFields(SongDTO songDTO){
        final ErrorResponse errorResponse = new ErrorResponse();
        if(Objects.isNull(songDTO.getName()) || songDTO.getName().isEmpty()){
            errorResponse.setErrorCode("400");
            errorResponse.setErrorMessage("Song name is required");
        }
        return errorResponse;
    }

    public static boolean isValidDuration(String duration){
        if(Objects.nonNull(duration) && duration.contains(":")){
            String[] durationParts = duration.split(":");
            return durationParts.length == 2 && durationParts[0].length() == 2 && durationParts[1].length() == 2;
        }
        return false;
    }
    public static boolean isValidYear(String year){
        final boolean isCorrectYear = Optional.ofNullable(year)
                                              .filter(v-> v.length()==4)
                                              .map(s-> s.chars().allMatch(Character::isDigit))
                                              .orElse(false);
        return isCorrectYear && (Integer.parseInt(year) > 1900 && Integer.valueOf(year) < 2099);
    }
}
