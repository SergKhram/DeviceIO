package io.github.sergkhram.data.repository;

import io.github.sergkhram.data.entity.Device;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceRepository extends MongoRepository<Device, String> {

    @Query("{'$or': [{'serial': {'$regex': /?0/, '$options': 'i'}},{'name': {'$regex': /?0/, '$options': 'i'}}]}")
    List<Device> search(@Param("searchTerm") String searchTerm);

    @Aggregation(pipeline = {
        "{'$lookup':{'from': 'host', 'localField': 'host', 'foreignField': '_id', 'as': 'aggregationField'}}",
        "{'$set':{'aggregationField': { $arrayElemAt: ['$aggregationField', 0] }}}",
        "{'$match':{'$and' :[{'$or': [{ 'serial': {'$regex': /?0/, '$options': 'i'}},{'name': {'$regex': /?0/, '$options': 'i'}}]},{'aggregationField.id': ?1},{'aggregationField.isActive': ?2}]}}"
    })
    List<Device> search(
        @Param("searchTerm") String searchTerm,
        @Param("hostId") String hostId,
        @Param("isActiveHost") Boolean isActiveHost
    );

    @Aggregation(pipeline = {
        "{'$lookup':{'from': 'host', 'localField': 'host', 'foreignField': '_id', 'as': 'aggregationField'}}",
        "{'$set':{'aggregationField': { $arrayElemAt: ['$aggregationField', 0] }}}",
        "{'$match':{'$and' :[{'$or': [{ 'serial': {'$regex': /?0/, '$options': 'i'}},{'name': {'$regex': /?0/, '$options': 'i'}}]},{'aggregationField.id': ?1}]}}"
    })
    List<Device> search(@Param("searchTerm") String searchTerm, @Param("hostId") String hostId);
}
