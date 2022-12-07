db.createUser(
  {
    user: 'root',
    pwd: 'password',
    roles: [{ role: 'readWrite', db: 'DeviceIO' }],
    mechanisms: ["SCRAM-SHA-1", "SCRAM-SHA-256"]
  }
);
db.createCollection('device');