/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.ui.permissions.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PermissionInfo;

/**
 * @author Andrei_Tytsik
 */
public class CsvPermissionExporter implements PermissionExporter {

    @Override
    public void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException {
        CSVPrinter csvPrinter = CSVFormat.DEFAULT.withHeader((String[]) HEADERS.toArray()).print(out);
        for (PermissionInfo permissionInfo : permissionInfos) {
            csvPrinter.printRecord(toCsvRecord(permissionInfo));
        }
    }

    private Object[] toCsvRecord(PermissionInfo permissionInfo) {
        Object[] record = new Object[HEADERS.size()];
        record[0] = permissionInfo.getKafkaPrincipal().toString();
        record[1] = permissionInfo.getResourceType().name();
        record[2] = permissionInfo.getResourceName();
        record[3] = permissionInfo.getPermissionType().name();
        record[4] = permissionInfo.getOperation().name();
        record[5] = permissionInfo.getHost();
        Metadata metadata = permissionInfo.getMetadata().orElse(null);
        record[6] = metadata != null ? metadata.getDescription() : null;
        return record;
    }

}
