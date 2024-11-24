package com.pumahawk.dbridge.util;

import java.util.Optional;
import javax.sql.DataSource;

public interface DatabaseConnector {
  Optional<? extends DataSource> getDefault();

  Optional<? extends DataSource> getById(String string);
}
