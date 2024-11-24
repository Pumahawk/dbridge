package com.pumahawk.dbridge.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.pumahawk.dbridge.configuration.BasicMetadata;
import com.pumahawk.dbridge.configuration.BasicResource;
import com.pumahawk.dbridge.configuration.DBridgeConfigResource;
import com.pumahawk.dbridge.configuration.DBridgeConfigSpec;
import com.pumahawk.dbridge.configuration.Database;
import com.pumahawk.dbridge.configuration.GlobalValidatorResource;
import com.pumahawk.dbridge.configuration.Kind;
import com.pumahawk.dbridge.configuration.QueryResource;
import com.pumahawk.dbridge.configuration.QuerySpec;
import com.pumahawk.dbridge.configuration.Spec;
import com.pumahawk.dbridge.configuration.Validator;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConfigurationStoreTests {

  @Autowired private YAMLMapper yamlMapper;

  @Test
  public void addAndGet() {
    ConfigurationStore cs = generateStoreWithElement(() -> new QueryResource(), "name-metadata");
    assertEquals("name-metadata", cs.get(Kind.QUERY).get("name-metadata").getMetadata().getName());
  }

  @Test
  public void addAndGetQueries() {
    ConfigurationStore cs = generateStoreWithElement(() -> new QueryResource(), "name-query");
    String name = cs.getQueries().findFirst().get().getMetadata().getName();
    assertEquals("name-query", name);
  }

  @Test
  public void addAndGetDBRidgeConfiguration() {
    ConfigurationStore cs =
        generateStoreWithElement(() -> new DBridgeConfigResource(), "name-dbridge-config");
    String name = cs.getDBridgeConfig().findFirst().get().getMetadata().getName();
    assertEquals("name-dbridge-config", name);
  }

  @Test
  public void getDatabase() {
    ConfigurationStore cs = new ConfigurationStore();
    DBridgeConfigResource config1 = new DBridgeConfigResource();
    BasicMetadata bs1 = new BasicMetadata();
    bs1.setName("config1");
    config1.setMetadata(bs1);
    DBridgeConfigSpec spec1 = new DBridgeConfigSpec();
    spec1.setDatabase(new LinkedList<>());
    Database db1 = new Database();
    db1.setConfigurationId("db1-id");
    db1.setName("db1");
    spec1.getDatabase().add(db1);
    Database db2 = new Database();
    db2.setConfigurationId("db2-id");
    db2.setName("db2");
    spec1.getDatabase().add(db2);
    cs.add(config1);
    config1.setSpec(spec1);
    BasicMetadata bs2 = new BasicMetadata();
    bs2.setName("config2");
    DBridgeConfigResource config2 = new DBridgeConfigResource();
    config2.setMetadata(bs2);
    DBridgeConfigSpec spec2 = new DBridgeConfigSpec();
    spec2.setDatabase(new LinkedList<>());
    Database db3 = new Database();
    db3.setConfigurationId("db3-id");
    db3.setName("db3");
    spec2.getDatabase().add(db3);
    config2.setSpec(spec2);
    cs.add(config2);

    List<Database> dbl = cs.getDatabase().collect(Collectors.toList());
    assertEquals(3, dbl.size());
    assertEquals("db3-id", dbl.get(0).getConfigurationId());
    assertEquals("db1-id", dbl.get(1).getConfigurationId());
    assertEquals("db2-id", dbl.get(2).getConfigurationId());
  }

  @Test
  public void solveExtensionSimple() throws IOException {
    ConfigurationStore cs = loadStore();
    List<Validator> validators = findValidatorByQueryName(cs, "qe1");
    assertEquals(3, validators.size());
    assertEquals("id", validators.get(0).getName());
    assertEquals("id2", validators.get(1).getName());
    assertEquals("basicExtension", validators.get(2).getName());
  }

  @Test
  public void solveExtensionMultiple() throws IOException {
    ConfigurationStore cs = loadStore();
    List<Validator> validators = findValidatorByQueryName(cs, "qe2");
    assertEquals(4, validators.size());
    assertEquals("namemyext", validators.get(0).getName());
    assertEquals("ext2Name", validators.get(1).getName());
    assertEquals("multipleExtension", validators.get(2).getName());
    assertEquals("query_2", validators.get(3).getName());
  }

  @Test
  public void solveExtensionMultipleCircular() throws IOException {
    ConfigurationStore cs = loadStore();
    List<Validator> validators = findValidatorByQueryName(cs, "qe4");
    assertEquals(6, validators.size());
    assertEquals("namemyext", validators.get(0).getName());
    assertEquals("ext2Name", validators.get(1).getName());
    assertEquals("multipleExtension", validators.get(2).getName());
    assertEquals("query_2", validators.get(3).getName());
    assertEquals("repeate_extension", validators.get(4).getName());
    assertEquals("empty-ext", validators.get(5).getName());
  }

  @Test
  public void solveExtensionMultiple_multipleCall() throws IOException {
    ConfigurationStore cs = loadStore();
    findValidatorByQueryName(cs, "qe2");
    List<Validator> validators = findValidatorByQueryName(cs, "qe2");
    assertEquals(4, validators.size());
    assertEquals("namemyext", validators.get(0).getName());
    assertEquals("ext2Name", validators.get(1).getName());
    assertEquals("multipleExtension", validators.get(2).getName());
    assertEquals("query_2", validators.get(3).getName());
  }

  @Test
  public void solveExtensionMultiple_multipleQueryExtensions() throws IOException {
    ConfigurationStore cs = loadStore();
    findValidatorByQueryName(cs, "qe2");
    List<Validator> validators = findValidatorByQueryName(cs, "qe2");
    assertEquals(4, validators.size());
    assertEquals("namemyext", validators.get(0).getName());
    assertEquals("ext2Name", validators.get(1).getName());
    assertEquals("multipleExtension", validators.get(2).getName());
    assertEquals("query_2", validators.get(3).getName());

    findValidatorByQueryName(cs, "qe3");
    validators = findValidatorByQueryName(cs, "qe3");
    assertEquals(4, validators.size());
    assertEquals("namemyext", validators.get(0).getName());
    assertEquals("ext2Name", validators.get(1).getName());
    assertEquals("multipleExtension", validators.get(2).getName());
    assertEquals("query_2", validators.get(3).getName());
  }

  private ConfigurationStore loadStore() throws IOException {
    ConfigurationStore cs = new ConfigurationStore();
    JsonNode config = yamlMapper.readTree(getClass().getResource("configuration-store-tests.yaml"));
    List<GlobalValidatorResource> gvs =
        yamlMapper.convertValue(
            config.get("global"), new TypeReference<List<GlobalValidatorResource>>() {});
    List<QueryResource> qs =
        yamlMapper.convertValue(config.get("queries"), new TypeReference<List<QueryResource>>() {});
    gvs.forEach(cs::add);
    qs.forEach(cs::add);
    return cs;
  }

  private List<Validator> findValidatorByQueryName(ConfigurationStore cs, String name) {
    return cs.getQueries()
        .filter(r -> name.equals(r.getMetadata().getName()))
        .map(QueryResource::getSpec)
        .map(QuerySpec::getValidators)
        .findAny()
        .get();
  }

  private ConfigurationStore generateStoreWithElement(
      Supplier<BasicResource<? extends Spec>> supplyResource, String name) {
    ConfigurationStore cs = new ConfigurationStore();
    BasicResource<? extends Spec> rs = supplyResource.get();
    BasicMetadata metadata = new BasicMetadata();
    metadata.setName(name);
    rs.setMetadata(metadata);
    cs.add(rs);
    return cs;
  }
}
