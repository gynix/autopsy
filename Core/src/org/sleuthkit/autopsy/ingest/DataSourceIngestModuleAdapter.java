/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.ingest;

import org.sleuthkit.datamodel.Content;

/**
 * An adapter that provides a no-op implementation of the startUp() method for
 * data source ingest modules.
 *
 * NOTE: As of Java 8, interfaces can have default methods.
 * DataSourceIngestModule now provides default no-op versions of startUp() and
 * shutDown(). This class is no longer needed and can be deprecated when
 * convenient.
 */
public abstract class DataSourceIngestModuleAdapter implements DataSourceIngestModule {

    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
    }

    @Override
    public abstract ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar);

}
