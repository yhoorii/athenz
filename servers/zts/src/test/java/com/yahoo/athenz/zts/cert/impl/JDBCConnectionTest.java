/**
 * Copyright 2017 Yahoo Inc.
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
package com.yahoo.athenz.zts.cert.impl;

import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;

import com.yahoo.athenz.common.server.db.PoolableDataSource;
import com.yahoo.athenz.zts.cert.X509CertRecord;

import junit.framework.TestCase;

public class JDBCConnectionTest extends TestCase {
    
    @Mock PoolableDataSource mockDataSrc;
    @Mock Statement mockStmt;
    @Mock PreparedStatement mockPrepStmt;
    @Mock Connection mockConn;
    @Mock ResultSet mockResultSet;
    @Mock JDBCCertRecordStoreConnection mockJDBCConn;
    
    JDBCCertRecordStore strStore;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(mockConn).when(mockDataSrc).getConnection();
        Mockito.doReturn(mockStmt).when(mockConn).createStatement();
        Mockito.doReturn(mockResultSet).when(mockPrepStmt).executeQuery();
        Mockito.doReturn(mockPrepStmt).when(mockConn).prepareStatement(Matchers.isA(String.class));
        Mockito.doReturn(true).when(mockStmt).execute(Matchers.isA(String.class));
    }
    
    @Test
    public void testGetX509CertRecord() throws Exception {

        Date now = new Date();
        Timestamp tstamp = new Timestamp(now.getTime());
        Mockito.when(mockResultSet.next()).thenReturn(true);
        Mockito.doReturn("cn").when(mockResultSet).getString(JDBCCertRecordStoreConnection.DB_COLUMN_CN);
        Mockito.doReturn("current-serial").when(mockResultSet).getString(JDBCCertRecordStoreConnection.DB_COLUMN_CURRENT_SERIAL);
        Mockito.doReturn("current-ip").when(mockResultSet).getString(JDBCCertRecordStoreConnection.DB_COLUMN_CURRENT_IP);
        Mockito.doReturn(tstamp).when(mockResultSet).getTimestamp(JDBCCertRecordStoreConnection.DB_COLUMN_CURRENT_TIME);
        Mockito.doReturn("prev-serial").when(mockResultSet).getString(JDBCCertRecordStoreConnection.DB_COLUMN_PREV_SERIAL);
        Mockito.doReturn("prev-ip").when(mockResultSet).getString(JDBCCertRecordStoreConnection.DB_COLUMN_PREV_IP);
        Mockito.doReturn(tstamp).when(mockResultSet).getTimestamp(JDBCCertRecordStoreConnection.DB_COLUMN_PREV_TIME);
        
        JDBCCertRecordStoreConnection jdbcConn = new JDBCCertRecordStoreConnection(mockConn, false);
        X509CertRecord certRecord = jdbcConn.getX509CertRecord("instance-id");
        
        assertNotNull(certRecord);
        assertEquals(certRecord.getCn(), "cn");
        assertEquals(certRecord.getCurrentIP(), "current-ip");
        assertEquals(certRecord.getCurrentSerial(), "current-serial");
        assertEquals(certRecord.getCurrentTime(), now);
        assertEquals(certRecord.getInstanceId(), "instance-id");
        assertEquals(certRecord.getPrevIP(), "prev-ip");
        assertEquals(certRecord.getPrevSerial(), "prev-serial");
        assertEquals(certRecord.getPrevTime(), now);
        
        jdbcConn.close();
    }
    
    @Test
    public void testGetX509CertRecordNotFound() throws Exception {

        Mockito.when(mockResultSet.next()).thenReturn(false);

        JDBCCertRecordStoreConnection jdbcConn = new JDBCCertRecordStoreConnection(mockConn, false);
        X509CertRecord certRecord = jdbcConn.getX509CertRecord("instance-id-not-found");
        assertNull(certRecord);
        jdbcConn.close();
    }
    
    @Test
    public void testInsertX509Record() throws Exception {
        
        JDBCCertRecordStoreConnection jdbcConn = new JDBCCertRecordStoreConnection(mockConn, true);

        X509CertRecord certRecord = new X509CertRecord();
        Date now = new Date();

        certRecord.setCn("cn");
        certRecord.setInstanceId("instance-id");
        certRecord.setCurrentIP("current-ip");
        certRecord.setCurrentSerial("current-serial");
        certRecord.setCurrentTime(now);
        certRecord.setPrevIP("prev-ip");
        certRecord.setPrevSerial("prev-serial");
        certRecord.setPrevTime(now);

        Mockito.doReturn(1).when(mockPrepStmt).executeUpdate();
        boolean requestSuccess = jdbcConn.insertX509CertRecord(certRecord);
        assertTrue(requestSuccess);
        
        Mockito.verify(mockPrepStmt, times(1)).setString(1, "instance-id");
        Mockito.verify(mockPrepStmt, times(1)).setString(2, "cn");
        Mockito.verify(mockPrepStmt, times(1)).setString(3, "current-serial");
        Mockito.verify(mockPrepStmt, times(1)).setTimestamp(4, new java.sql.Timestamp(now.getTime()));
        Mockito.verify(mockPrepStmt, times(1)).setString(5, "current-ip");
        Mockito.verify(mockPrepStmt, times(1)).setString(6, "prev-serial");
        Mockito.verify(mockPrepStmt, times(1)).setTimestamp(7, new java.sql.Timestamp(now.getTime()));
        Mockito.verify(mockPrepStmt, times(1)).setString(8, "prev-ip");
        jdbcConn.close();
    }
    
    @Test
    public void testUpdateX509Record() throws Exception {
        
        JDBCCertRecordStoreConnection jdbcConn = new JDBCCertRecordStoreConnection(mockConn, true);

        X509CertRecord certRecord = new X509CertRecord();
        Date now = new Date();
        
        certRecord.setCn("cn");
        certRecord.setInstanceId("instance-id");
        certRecord.setCurrentIP("current-ip");
        certRecord.setCurrentSerial("current-serial");
        certRecord.setCurrentTime(now);
        certRecord.setPrevIP("prev-ip");
        certRecord.setPrevSerial("prev-serial");
        certRecord.setPrevTime(now);

        Mockito.doReturn(1).when(mockPrepStmt).executeUpdate();
        boolean requestSuccess = jdbcConn.updateX509CertRecord(certRecord);
        assertTrue(requestSuccess);
        
        Mockito.verify(mockPrepStmt, times(1)).setString(1, "current-serial");
        Mockito.verify(mockPrepStmt, times(1)).setTimestamp(2, new java.sql.Timestamp(now.getTime()));
        Mockito.verify(mockPrepStmt, times(1)).setString(3, "current-ip");
        Mockito.verify(mockPrepStmt, times(1)).setString(4, "prev-serial");
        Mockito.verify(mockPrepStmt, times(1)).setTimestamp(5, new java.sql.Timestamp(now.getTime()));
        Mockito.verify(mockPrepStmt, times(1)).setString(6, "prev-ip");
        Mockito.verify(mockPrepStmt, times(1)).setString(7, "instance-id");

        jdbcConn.close();
    }
}
