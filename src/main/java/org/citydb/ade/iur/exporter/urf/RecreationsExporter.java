package org.citydb.ade.iur.exporter.urf;

import org.citydb.ade.exporter.ADEExporter;
import org.citydb.ade.exporter.CityGMLExportHelper;
import org.citydb.ade.iur.exporter.ExportManager;
import org.citydb.ade.iur.schema.ADETable;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.ade.iur.model.module.UrbanFunctionModule;
import org.citygml4j.ade.iur.model.urf.Recreations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecreationsExporter implements ADEExporter {
    private final PreparedStatement ps;
    private final String module;
    private final UrbanFunctionExporter urbanFunctionExporter;

    public RecreationsExporter(Connection connection, CityGMLExportHelper helper, ExportManager manager) throws CityGMLExportException, SQLException {
        String tableName = manager.getSchemaMapper().getTableName(ADETable.RECREATIONS);
        module = UrbanFunctionModule.v1_3.getNamespaceURI();

        Table table = new Table(helper.getTableNameWithSchema(tableName));
        Select select = new Select().addProjection(table.getColumns("capacity", "numberofusers"))
                .addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());

        urbanFunctionExporter = manager.getExporter(UrbanFunctionExporter.class);
    }

    public void doExport(Recreations recreations, long objectId, AbstractType<?> objectType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
        ps.setLong(1, objectId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                urbanFunctionExporter.doExport(recreations, objectId, objectType, projectionFilter);

                if (projectionFilter.containsProperty("capacity", module)) {
                    int capacity = rs.getInt("capacity");
                    if (!rs.wasNull())
                        recreations.setCapacity(capacity);
                }

                if (projectionFilter.containsProperty("numberOfUsers", module)) {
                    int numberOfUsers = rs.getInt("numberofusers");
                    if (!rs.wasNull())
                        recreations.setNumberOfUsers(numberOfUsers);
                }
            }
        }
    }

    @Override
    public void close() throws CityGMLExportException, SQLException {
        ps.close();
    }
}
