package org.citydb.ade.iur.importer.urf;

import org.citydb.ade.importer.ADEImporter;
import org.citydb.ade.importer.CityGMLImportHelper;
import org.citydb.ade.importer.ForeignKeys;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.ade.iur.importer.ImportManager;
import org.citydb.ade.iur.schema.ADETable;
import org.citygml4j.ade.iur.model.urf.DisasterDamage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DisasterDamageImporter implements ADEImporter {
    private final CityGMLImportHelper helper;
    private final PreparedStatement ps;
    private final UrbanFunctionImporter urbanFunctionImporter;

    private int batchCounter;

    public DisasterDamageImporter(Connection connection, CityGMLImportHelper helper, ImportManager manager) throws CityGMLImportException, SQLException {
        this.helper = helper;

        ps = connection.prepareStatement("insert into " +
                helper.getTableNameWithSchema(manager.getSchemaMapper().getTableName(ADETable.DISASTERDAMAGE)) + " " +
                "(id, damagedarea, damagedarea_uom, maximumrainfallperhour, numberofdamagedhouses, " +
                "numberofhousesfloodedabovefl, numberofhousesfloodedbelowfl, totalrainfall) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?)");

        urbanFunctionImporter = manager.getImporter(UrbanFunctionImporter.class);
    }

    public void doImport(DisasterDamage disasterDamage, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException {
        urbanFunctionImporter.doImport(disasterDamage, objectId, objectType, foreignKeys);
        ps.setLong(1, objectId);

        if (disasterDamage.getDamagedArea() != null && disasterDamage.getDamagedArea().isSetValue()) {
            ps.setDouble(2, disasterDamage.getDamagedArea().getValue());
            ps.setString(3, disasterDamage.getDamagedArea().getUom());
        } else {
            ps.setNull(2, Types.DOUBLE);
            ps.setNull(3, Types.VARCHAR);
        }

        if (disasterDamage.getMaximumRainfallPerHour() != null)
            ps.setInt(4, disasterDamage.getMaximumRainfallPerHour());
        else
            ps.setNull(4, Types.INTEGER);

        if (disasterDamage.getNumberOfDamagedHouses() != null)
            ps.setInt(5, disasterDamage.getNumberOfDamagedHouses());
        else
            ps.setNull(5, Types.INTEGER);

        if (disasterDamage.getNumberOfHousesFloodedAboveFloorLevel() != null)
            ps.setInt(6, disasterDamage.getNumberOfHousesFloodedAboveFloorLevel());
        else
            ps.setNull(6, Types.INTEGER);

        if (disasterDamage.getNumberOfHousesFloodedBelowFloorLevel() != null)
            ps.setInt(7, disasterDamage.getNumberOfHousesFloodedBelowFloorLevel());
        else
            ps.setNull(7, Types.INTEGER);

        if (disasterDamage.getTotalRainfall() != null)
            ps.setInt(8, disasterDamage.getTotalRainfall());
        else
            ps.setNull(8, Types.INTEGER);

        ps.addBatch();
        if (++batchCounter == helper.getDatabaseAdapter().getMaxBatchSize())
            helper.executeBatch(objectType);
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            ps.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        ps.close();
    }
}
