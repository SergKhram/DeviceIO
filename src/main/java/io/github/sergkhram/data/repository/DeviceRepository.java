package io.github.sergkhram.data.repository;

import io.github.sergkhram.data.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    @Query("select d from Device d LEFT JOIN Host h ON d.host=h.id " +
        "where lower(d.serial) like lower(concat('%', :searchTerm, '%')) ")
    List<Device> search(@Param("searchTerm") String searchTerm);

    @Query("select d from Device d LEFT JOIN Host h ON d.host=h.id " +
        "where lower(d.serial) like lower(concat('%', :searchTerm, '%')) " +
        "and lower(d.host) like lower(concat('%', :hostId, '%')) " +
        "and h.isActive = :isActiveHost")
    List<Device> search(
        @Param("searchTerm") String searchTerm,
        @Param("hostId") String hostId,
        @Param("isActiveHost") Boolean isActiveHost
    );

    @Query("select d from Device d LEFT JOIN Host h ON d.host=h.id " +
        "where lower(d.serial) like lower(concat('%', :searchTerm, '%')) " +
        "and lower(d.host) like lower(concat('%', :hostId, '%'))")
    List<Device> search(@Param("searchTerm") String searchTerm, @Param("hostId") String hostId);
}
