package com.example.songservice.service;

import com.example.songservice.dto.SongDTO;
import com.example.songservice.entity.SongEntity;
import com.example.songservice.model.ErrorDetails;
import com.example.songservice.model.ErrorResponse;
import com.example.songservice.model.SimpleErrorResponse;
import com.example.songservice.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class SongService {
    @Autowired
    private SongRepository songRepository;

    public SongEntity addSong(SongEntity songEntity) {
        return this.songRepository.save(songEntity);
    }

    public Optional<SongEntity> getSong(final Integer id) {
        return this.songRepository.findById(id);
    }

    public void deleteSong(final Integer id) {
        this.songRepository.deleteById(id);

    }
    public ErrorResponse checkValidity(SongDTO songDTO){
        final ErrorResponse errorResponse = new ErrorResponse();
        final ErrorDetails errorDetails = new ErrorDetails();
        if(!isValidYear(songDTO.getYear())){
            errorResponse.setErrorCode("400");
            errorResponse.setErrorMessage("Validation error");
            errorDetails.setYear(String.format("%s Year must be in YYYY format", songDTO.getYear()));
        }
        if(!isValidDuration(songDTO.getLength())){
            errorResponse.setErrorCode("400");
            errorResponse.setErrorMessage("Validation error");
            errorDetails.setDuration(String.format("%s Duration must be in MM:SS format", songDTO.getLength()));
        }
        errorResponse.setErrorDetails(errorDetails);
        return errorResponse;
    }

    public SimpleErrorResponse checkMissingFields(SongDTO songDTO){
        final SimpleErrorResponse errorResponse = new SimpleErrorResponse();
        if(Objects.isNull(songDTO.getName()) || songDTO.getName().isEmpty()){
            errorResponse.setErrorCode("400");
            errorResponse.setErrorMessage("Name field is mandatory");
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
        return Objects.nonNull(year) && year.length() == 4;
    }
}
