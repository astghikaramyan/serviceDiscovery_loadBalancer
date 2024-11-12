package com.example.songservice.service;

import com.example.songservice.entity.SongEntity;
import com.example.songservice.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
