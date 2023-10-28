package com.htc.mds.blobstorage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.microsoft.bot.integration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;


public class BlobStorageClient {

    private String _storageConnectionString;
    private String _blobContainName;
    private Configuration _withConfiguration;
    private CloudBlobClient _blobClient = null;
    private CloudBlobContainer _container = null;

    Logger logger = LoggerFactory.getLogger(getClass());

    public BlobStorageClient(Configuration configuration) {
        _withConfiguration = configuration;
        _storageConnectionString = _withConfiguration.getProperty("StorageBlobConnectionString");
        _blobContainName = _withConfiguration.getProperty("BlobContainName");

    }

    public void init() {
        try {
            logger.info("BlobStorageClient init begin");
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(_storageConnectionString);
            _blobClient = storageAccount.createCloudBlobClient();
            _container = _blobClient.getContainerReference(_blobContainName);

            _container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());
            logger.info("BlobStorageClient init end");

        } catch (StorageException e) {
            logger.info("BlobStorageClient init failed");
            logger.error(e.getMessage(), e);
        } catch (URISyntaxException e) {
            logger.info("BlobStorageClient init failed");
            logger.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            logger.info("BlobStorageClient init failed");
            logger.error(e.getMessage(), e);
        } catch (Exception ex) {
            logger.info("BlobStorageClient init failed");
            logger.error(ex.getMessage(), ex);
        }

    }

    public boolean uploadFile(String guid, InputStream inputStream, long length) {
        try {
            logger.info("uploadFile begin guid:" + guid);
            CloudBlockBlob blob = _container.getBlockBlobReference(guid);
            blob.upload(inputStream, length);
            logger.info("uploadFile end guid:" + guid);
            return true;

        } catch (URISyntaxException ex) {
            logger.info("uploadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (StorageException ex) {
            logger.info("uploadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.info("uploadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.info("uploadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        }
        return false;

    }

    public boolean uploadText(String guid, String content) {
        try {
            logger.info("uploadText begin guid:" + guid);
            CloudBlockBlob blob = _container.getBlockBlobReference(guid);
            blob.uploadText(content);
            logger.info("uploadText end guid:" + guid);
            return true;

        } catch (URISyntaxException ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (StorageException ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }

    public String downloadText(String guid) {

        try {
            logger.info("uploadText begin guid:" + guid);
            CloudBlockBlob blob = _container.getBlockBlobReference(guid);
            String content = blob.downloadText();
            logger.info("uploadText end guid:" + guid);
            return content;

        } catch (URISyntaxException ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        } catch (StorageException ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        } catch (IOException ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        } catch (Exception ex) {
            logger.info("uploadText failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        }

    }

    public InputStream getInputStream(String guid) {
        try {
            logger.info("getInputStream begin guid:" + guid);
            CloudBlockBlob blob = _container.getBlockBlobReference(guid);
            return blob.openInputStream();

        } catch (URISyntaxException ex) {
            logger.info("getInputStream failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (StorageException ex) {
            logger.info("getInputStream failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.info("getInputStream failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        }

        return null;
    }

    public void downloadFile(String guid, OutputStream outStream) {
        try {
            logger.info("downloadFile begin guid:" + guid);
            CloudBlockBlob blob = _container.getBlockBlobReference(guid);
            blob.download(outStream);
            logger.info("downloadFile end guid:" + guid);
        } catch (URISyntaxException ex) {
            logger.info("downloadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (StorageException ex) {
            logger.info("downloadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.info("downloadFile failed guid:" + guid);
            logger.error(ex.getMessage(), ex);
        }
    }


}
