/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.util.Providers;
import org.apache.druid.client.DruidServerConfig;
import org.apache.druid.discovery.DataNodeService;
import org.apache.druid.guice.annotations.Self;
import org.apache.druid.query.DruidProcessingConfig;
import org.apache.druid.segment.column.ColumnConfig;
import org.apache.druid.segment.loading.SegmentLoaderConfig;
import org.apache.druid.server.DruidNode;
import org.apache.druid.server.coordination.DruidServerMetadata;

import javax.annotation.Nullable;

/**
 */
public class StorageNodeModule implements Module
{
  @Override
  public void configure(Binder binder)
  {
    JsonConfigProvider.bind(binder, "druid.server", DruidServerConfig.class);
    JsonConfigProvider.bind(binder, "druid.segmentCache", SegmentLoaderConfig.class);

    binder.bind(ServerTypeConfig.class).toProvider(Providers.of(null));
    binder.bind(ColumnConfig.class).to(DruidProcessingConfig.class);
  }

  @Provides
  @LazySingleton
  public DruidServerMetadata getMetadata(
      @Self DruidNode node,
      @Nullable ServerTypeConfig serverTypeConfig,
      DruidServerConfig config
  )
  {
    if (serverTypeConfig == null) {
      throw new ProvisionException("Must override the binding for ServerTypeConfig if you want a DruidServerMetadata.");
    }

    return new DruidServerMetadata(
        node.getHostAndPortToUse(),
        node.getHostAndPort(),
        node.getHostAndTlsPort(),
        config.getMaxSize(),
        serverTypeConfig.getServerType(),
        config.getTier(),
        config.getPriority()
    );
  }

  @Provides
  @LazySingleton
  public DataNodeService getDataNodeService(@Nullable ServerTypeConfig serverTypeConfig, DruidServerConfig config)
  {
    if (serverTypeConfig == null) {
      throw new ProvisionException("Must override the binding for ServerTypeConfig if you want a DruidServerMetadata.");
    }

    return new DataNodeService(
        config.getTier(),
        config.getMaxSize(),
        serverTypeConfig.getServerType(),
        config.getPriority()
    );
  }
}
