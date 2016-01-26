package io.ddf2.bigquery.preparer;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.*;
import io.ddf2.bigquery.BQDataSource;
import io.ddf2.bigquery.BigQueryContext;
import io.ddf2.bigquery.BigQueryUtils;
import io.ddf2.datasource.IDataSource;
import io.ddf2.datasource.IDataSourcePreparer;
import io.ddf2.datasource.PrepareDataSourceException;
import io.ddf2.datasource.SqlDataSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sangdn on 1/22/16.
 * BigQueryPreparer will prepare for BigQueryDDF
 * 1/ Create VIEW from BigQueryDataSource
 * 2/ Parse respone to get Schema
 * 3/ Required:
 */
public class BigQueryPreparer implements IDataSourcePreparer {
    protected Bigquery bigquery;
    public static final String TMP_VIEW_DATASET_ID = "tmp_view_ddf";


    public BigQueryPreparer(Bigquery bigquerry) {
        this.bigquery = bigquerry;


    }

    @Override
    public IDataSource prepare(String ddfName, IDataSource dataSource) throws PrepareDataSourceException {
        try {
            ensureTmpViewDataSet();
            BQDataSource datasource = (BQDataSource) dataSource;
            assert datasource != null;
            Table table = new Table();
            TableReference tableReference = new TableReference();
            tableReference.setTableId(ddfName);
            tableReference.setProjectId(datasource.getProjectId());
            tableReference.setDatasetId(TMP_VIEW_DATASET_ID);

            table.setTableReference(tableReference);

            ViewDefinition viewDefinition = new ViewDefinition();
            viewDefinition.setQuery(datasource.getQuery());
            table.setView(viewDefinition);
//            table.setExpirationTime(TimeUnit.DAYS.toMillis(1));

            Table tblResponse = bigquery.tables().insert(datasource.getProjectId(), TMP_VIEW_DATASET_ID, table).execute();

            return BQDataSource.builder().setProjectId(((BQDataSource) dataSource).getProjectId())
                    .setNumRows(tblResponse.getNumRows().longValue())
                    .setCreatedTime(tblResponse.getCreationTime())
                    .setSchema(BigQueryUtils.convertToDDFSchema(tblResponse.getSchema()))
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            throw new PrepareDataSourceException(e.getMessage());
        }
    }

    private void ensureTmpViewDataSet() throws IOException, PrepareDataSourceException {
        boolean isFound = false;
        DatasetList list = bigquery.datasets().list(BigQueryContext.getProjectId()).execute();
        for (DatasetList.Datasets ds :list.getDatasets()){
            String datasetId = ds.getDatasetReference().getDatasetId();
            if(datasetId.equals(TMP_VIEW_DATASET_ID)){
                isFound = true; break;
            }
        }
        if(isFound == false){
            DatasetReference ref = new DatasetReference();
            ref.setDatasetId(TMP_VIEW_DATASET_ID);
            Dataset dataset = new Dataset();
            dataset.setDatasetReference(ref);
            dataset = bigquery.datasets()
                    .insert(BigQueryContext.getProjectId(),dataset).execute();
            if(dataset == null || dataset.getDatasetReference().getDatasetId().equals(TMP_VIEW_DATASET_ID) == false){
                throw new PrepareDataSourceException("Couldn't Create Temprory Dataset to store View");
            }
        }

    }
}
