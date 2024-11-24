package com.pumahawk.dbridge.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pumahawk.dbridge.configuration.ConfigurationException;
import com.pumahawk.dbridge.configuration.ConfigurationResource;
import com.pumahawk.dbridge.configuration.Kind;
import com.pumahawk.dbridge.util.DBridgeConstants.Properties;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationLoader {

  @Autowired private ObjectMapper yamlMapper;

  private final File configurationDirectory;

  public ConfigurationLoader(Environment env, ResourceLoader resourceLoader) {
    this.configurationDirectory =
        Optional.ofNullable(env.getProperty(Properties.CONFIGURATION_PATH))
            .map(resourceLoader::getResource)
            .map(
                r -> {
                  try {
                    return r.getFile();
                  } catch (IOException e) {
                    throw new ConfigurationException(
                        "Unable to get file from resource. Resource path: " + r.getFilename(), e);
                  }
                })
            .filter(File::isDirectory)
            .orElseThrow(
                () -> new ConfigurationException("Unable get retrieve configuration directory"));
  }

  public List<JsonNode> getAllConfigurations() {
    return getAllConfigurations(new File[] {configurationDirectory});
  }

  public ConfigurationStore getConfigurationStore() {
    ConfigurationStore store = new ConfigurationStore();
    getAllConfigurations().forEach(n -> store.add(configFromNode(n)));
    return store;
  }

  private ConfigurationResource<? extends Object> configFromNode(JsonNode node) {
    return new ObjectMapper()
        .convertValue(
            node,
            (Class<? extends ConfigurationResource<?>>)
                Optional.of(node)
                    .map(n -> n.get("kind"))
                    .map(JsonNode::asText)
                    .map(Kind::fromName)
                    .map(Kind::getType)
                    .orElseThrow(
                        () ->
                            new ConfigurationException(
                                "Unable to retrieve kind from configuration node")));
  }

  private List<JsonNode> getAllConfigurations(File[] files) {
    LinkedList<JsonNode> list = new LinkedList<>();
    for (File in : files) {
      if (in.isDirectory()) {
        list.addAll(getAllConfigurations(in.listFiles()));
      } else if (in.getName().endsWith(".dbridge.yaml")) {
        try {
          list.add(yamlMapper.readTree(in));
        } catch (IOException e) {
          throw new ConfigurationException(
              "unable to load configuration file. name: " + in.getName(), e);
        }
      }
    }
    return list;
  }
}
