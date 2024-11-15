package com.example.songservice.mapper;

import com.example.songservice.dto.SongDTO;
import com.example.songservice.entity.SongEntity;
import org.springframework.stereotype.Service;

@Service
public class SongMapper {
    public SongEntity mapToEntity(SongDTO songDTO) {
        final SongEntity songEntity = new SongEntity();
        songEntity.setName(songDTO.getName());
        songEntity.setArtist(songDTO.getArtist());
        songEntity.setAlbum(songDTO.getAlbum());
        songEntity.setDuration(songDTO.getDuration());
        songEntity.setYear(songDTO.getYear());
        songEntity.setResourceId(songDTO.getResourceId());
        return songEntity;
    }

    public SongDTO mapToDTO(SongEntity songEntity) {
        final SongDTO songDTO = new SongDTO();
        songDTO.setId(songEntity.getId());
        songDTO.setName(songEntity.getName());
        songDTO.setArtist(songEntity.getArtist());
        songDTO.setAlbum(songEntity.getAlbum());
        songDTO.setDuration(songEntity.getDuration());
        songDTO.setYear(songEntity.getYear());
        songDTO.setResourceId(songEntity.getResourceId());
        return songDTO;
    }
}
