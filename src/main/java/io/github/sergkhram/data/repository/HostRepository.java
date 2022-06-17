package io.github.sergkhram.data.repository;

import io.github.sergkhram.data.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HostRepository extends JpaRepository<Host, UUID> {
    @Query("select h from Host h " +
        "where lower(h.name) like lower(concat('%', :searchTerm, '%')) " +
        "or lower(h.address) like lower(concat('%', :searchTerm, '%'))")
    List<Host> search(@Param("searchTerm") String searchTerm);

    @Query("select h from Host h " +
        "where (lower(h.name) like lower(concat('%', :searchTerm, '%')) " +
        "or lower(h.address) like lower(concat('%', :searchTerm, '%'))) " +
        "and h.isActive = :searchIsActive")
    List<Host> search(@Param("searchTerm") String searchTerm, @Param("searchIsActive") Boolean searchIsActive);
}
