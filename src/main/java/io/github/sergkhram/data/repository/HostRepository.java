package io.github.sergkhram.data.repository;

import io.github.sergkhram.data.entity.Host;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HostRepository extends MongoRepository<Host, String> {

    @Query("{$or:[{address: {'$regex': /?0/, '$options': 'i'} },{name: {'$regex': /?0/, '$options': 'i'}}]}")
    List<Host> search(@Param("searchTerm") String searchTerm);

    @Query("{$and:[{$or:[{address: {'$regex': /?0/, '$options': 'i'} },{name: {'$regex': /?0/, '$options': 'i'}}]},{isActive: ?1 }]}")
    List<Host> search(@Param("searchTerm") String searchTerm, @Param("searchIsActive") Boolean searchIsActive);
}
