package org.eclipse.persistence.mappings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import junit.framework.Assert;

import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.internal.databaseaccess.Platform;
import org.eclipse.persistence.internal.helper.ConversionManager;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.identitymaps.CacheId;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.ArrayRecord;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.queries.BatchFetchPolicy;
import org.eclipse.persistence.queries.ComplexQueryResult;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ForeignReferenceMappingTest {

	private static final DatabaseField TAB1_KEY = new DatabaseField("TAB1.ID");
	private static final DatabaseField TAB2_FOR_KEY = new DatabaseField("TAB2.TAB1_ID");
	
	// Capture foreign keys computed for capturing:
	private ArgumentCaptor<List> captureForeignKeys;
	
	@Test
	public void testForeignReferenceMapping_extractResultFromBatchQuery_2_from_10() {
		for (int startIndex = 0; startIndex < 10; startIndex++) {
			testForeignReferenceMapping_extractResultFromBatchQuery(10, startIndex, 2);
		}
	}

	@Test
	public void testForeignReferenceMapping_extractResultFromBatchQuery_2_from_2() {
		testForeignReferenceMapping_extractResultFromBatchQuery(2, 0, 2);
		testForeignReferenceMapping_extractResultFromBatchQuery(2, 1, 2);
	}

	@Test
	public void testForeignReferenceMapping_extractResultFromBatchQuery_2_from_1() {
		testForeignReferenceMapping_extractResultFromBatchQuery(1, 0, 2);
	}

	private void testForeignReferenceMapping_extractResultFromBatchQuery(final int numberOfRows, final int startIndex, final int batchFetchPolicySize) {
		final OneToManyMapping frm = createOneToManyMapping();
		final ArrayRecord[] sourceRows = createSourceRows(numberOfRows);
		final ReadAllQuery batchQuery = createBatchQuery(batchFetchPolicySize);
		final ArrayRecord sourceRow = sourceRows[startIndex];
		final AbstractSession session = buildSession();
		final ReadAllQuery originalQuery = createOriginalQuery(batchFetchPolicySize, sourceRows);
		
		frm.extractResultFromBatchQuery(batchQuery, null, sourceRow, session, originalQuery);
		
		// Compute foreign keys of source rows:
		final Set foreignKeysOfSourceRows = new HashSet();
		for (ArrayRecord row : sourceRows) {
			final Object foreignKey = extractForeignKey(frm, session, row);
			if (foreignKey != null) {
				foreignKeysOfSourceRows.add(foreignKey);
			}
		}
		
		final int numberOfForeignKeysExpected = numberOfRows < batchFetchPolicySize ? numberOfRows : batchFetchPolicySize;
		
		// Check foreign keys:
		final List foreignKeys = captureForeignKeys.getValue();
		Assert.assertNotNull(foreignKeys);
		Assert.assertEquals("Number of foreign keys", numberOfForeignKeysExpected, foreignKeys.size());
		
		// Check that all foreign keys belong to original source rows:
		for (Object foreignKey : foreignKeys) {
			Assert.assertTrue("Foreign key not belonging to source row: "+foreignKey, foreignKeysOfSourceRows.remove(foreignKey));
		}
		// Now foreignKeysOfSourceRows does not contain foreign keys for batch query any more
		
		// Check remaining rows:
		final List remainingRows = originalQuery.getBatchFetchPolicy().getDataResults().get(frm);
		Assert.assertNotNull(remainingRows);
		Assert.assertEquals("Number of remaining rows", sourceRows.length-numberOfForeignKeysExpected, remainingRows.size());
		
		// Check that remaining rows belong to source rows and that a foreign key is not contained any more:
		for (Object row : remainingRows) {
			final Object foreignKey = extractForeignKey(frm, session, (AbstractRecord)row);
			if (foreignKey != null) {
				Assert.assertTrue("One of the remaining rows is one of the fetched foreign keys or does not belong to source rows or is contained twice: "+foreignKey, foreignKeysOfSourceRows.remove(foreignKey));
			}
		}
		
		// Check that foreign keys and remaining rows do cover all foreign keys:
		Assert.assertTrue("Foreign keys of source rows not covered by foreign keys and remaining rows: "+foreignKeysOfSourceRows, foreignKeysOfSourceRows.isEmpty());
	}

	private Object extractForeignKey(final ForeignReferenceMapping frm, final AbstractSession session, final AbstractRecord row) {
		final Object foreignKey = frm.extractBatchKeyFromRow(row, session);
		if (foreignKey == null) {
			return null;
		}
        final Object[] key = ((CacheId)foreignKey).getPrimaryKey();
        final Object foreignKeyValue = key[0];
        return foreignKeyValue;
	}

	private OneToManyMapping createOneToManyMapping() {
		final OneToManyMapping frm = new OneToManyMapping();
		frm.getSourceKeyFields().add(TAB1_KEY);
		frm.getTargetForeignKeyFields().add(TAB2_FOR_KEY);
		return frm;
	}

	private ArrayRecord[] createSourceRows(final int count) {
		final ArrayRecord[] rows = new ArrayRecord[count];
		for (int i = 0; i < count; i++) {
			rows[i] = createTab1Row(i);
		}
		return rows;
	}

	private ReadAllQuery createBatchQuery(final int batchFetchPolicySize) {
		final ReadAllQuery batchQuery = new ReadAllQuery();
		final BatchFetchPolicy batchFetchPolicy = new BatchFetchPolicy(BatchFetchType.IN);
		batchFetchPolicy.setSize(batchFetchPolicySize);
		batchQuery.setBatchFetchPolicy(batchFetchPolicy);
		return batchQuery;
	}
	
	private ReadAllQuery createOriginalQuery(final int batchFetcPolicySize, ArrayRecord... rows) {
		final ReadAllQuery batchQuery = new ReadAllQuery();
		
		// Batch fetch policy
		final BatchFetchPolicy batchFetchPolicy = new BatchFetchPolicy(BatchFetchType.IN);
		batchFetchPolicy.setSize(batchFetcPolicySize);
		for (final ArrayRecord row : rows) {
			batchFetchPolicy.addDataResults(row);
		}
		batchQuery.setBatchFetchPolicy(batchFetchPolicy);
		
		// TranslationRow to capture foreign keys
		final AbstractRecord translationRow = Mockito.mock(AbstractRecord.class);
		Mockito.when(translationRow.clone()).thenReturn(translationRow);
		this.captureForeignKeys = ArgumentCaptor.forClass(List.class);
		Mockito.when(translationRow.put(Mockito.eq(ForeignReferenceMapping.QUERY_BATCH_PARAMETER), this.captureForeignKeys.capture())).thenReturn(null);
		
		batchQuery.setTranslationRow(translationRow);
		
		return batchQuery;
	}

	private AbstractSession buildSession() {
		final AbstractSession session = Mockito.mock(AbstractSession.class);
		final Platform datasourcePlatform = Mockito.mock(Platform.class);
		Mockito.when(session.getDatasourcePlatform()).thenReturn(datasourcePlatform);
		final ConversionManager conversionManager = ConversionManager.getDefaultManager();
		Mockito.when(datasourcePlatform.getConversionManager()).thenReturn(conversionManager);
		// Query execution
		final ComplexQueryResult complexQueryResult = new ComplexQueryResult();
		complexQueryResult.setResult(new ArrayList());
		complexQueryResult.setData(new ArrayList());
		Mockito.when(session.executeQuery(Mockito.any(DatabaseQuery.class), Mockito.any(AbstractRecord.class))).thenReturn(complexQueryResult);
		return session;
	}

	private ArrayRecord createTab1Row(final long id) {
		final Object key = new Long(id);
		final Vector<DatabaseField> databaseFields = new Vector<DatabaseField>();
		databaseFields.add(TAB1_KEY);
		final ArrayRecord sourceRow = new ArrayRecord(databaseFields, new DatabaseField[]{TAB1_KEY}, new Object[] {key});
		return sourceRow;
	}
}
