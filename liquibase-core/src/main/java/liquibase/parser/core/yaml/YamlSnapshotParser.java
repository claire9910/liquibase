package liquibase.parser.core.yaml;

import liquibase.database.Database;
import liquibase.exception.LiquibaseParseException;
import liquibase.parser.SnapshotParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.RestoredDatabaseSnapshot;
import liquibase.util.StreamUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlSnapshotParser extends YamlParser implements SnapshotParser {

    @Override
    public DatabaseSnapshot parse(String path, Database database, ResourceAccessor resourceAccessor) throws LiquibaseParseException {
        Yaml yaml = new Yaml();

        try {
            InputStream stream = StreamUtil.singleInputStream(path, resourceAccessor);
            if (stream == null) {
                throw new LiquibaseParseException(path + " does not exist");
            }

            Map parsedYaml;
            try {
                parsedYaml = yaml.loadAs(stream, Map.class);
            } catch (Exception e) {
                throw new LiquibaseParseException("Syntax error in " + getSupportedFileExtensions()[0] + ": " + e.getMessage(), e);
            }

            Map rootList = (Map) parsedYaml.get("snapshot");
            if (rootList == null) {
                throw new LiquibaseParseException("Could not find root snapshot node");
            }

            DatabaseSnapshot snapshot = new RestoredDatabaseSnapshot(database);
            ParsedNode snapshotNode = new ParsedNode(null, "snapshot");
            snapshotNode.setValue(rootList);

            snapshot.load(snapshotNode, resourceAccessor);

            return snapshot;
        } catch (Throwable e) {
            if (e instanceof LiquibaseParseException) {
                throw (LiquibaseParseException) e;
            }
            throw new LiquibaseParseException(e);
        }
    }
}
