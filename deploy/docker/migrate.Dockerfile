FROM mysql:8.0

WORKDIR /app

COPY scripts/import_local_db.sh /app/scripts/import_local_db.sh
COPY sql/ /app/sql/

RUN chmod +x /app/scripts/import_local_db.sh

ENTRYPOINT ["/app/scripts/import_local_db.sh"]
