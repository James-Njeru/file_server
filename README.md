# File Server

## Overview

This is a Spring Boot application that provides file storage and management functionality. It allows users to upload, retrieve, delete, and list files with basic authentication and caching features. The service also includes an API for interacting with the files and their metadata.

## Features

- **File Upload**: Upload images, compress and store them in a designated directory.
- **File Retrieval**: Retrieve files based on their name.
- **File Deletion**: Delete files from storage.
- **File Metadata**: Save and manage file metadata in a PostgreSQL database.
- **Caching**: Cache file metadata and summary information to improve performance.
- **Authentication**: Basic form-based authentication with in-memory users.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL database


## File Operations

### Authentication

- **Login**: `POST /login`
  - **Username**: `user` (password: `password`) or `admin` (password: `admin`)

### File Endpoints

- **Upload File**: `POST /api/files/upload`
  - **Description**: Upload an image file, compress and store it in the configured directory.
  - **Request**:
    - **Headers**: `Content-Type: multipart/form-data`
    - **Form-data**:
      - `file` (required) - The file to be uploaded.
  - **Authentication**: Required

- **Delete File**: `DELETE /api/files/{filename}`
  - **Description**: Delete a file based on its filename.
  - **Path Parameter**:
    - `filename` - The name of the file to delete.
  - **Authentication**: Required

- **Get All Files Metadata**: `GET /api/files/metadata`
  - **Description**: Retrieve metadata for all uploaded files.
  - **Authentication**: Required

- **Get File Summary**: `GET /api/files/summary`
  - **Description**: Provides a summary of all files including total count and storage used.
  - **Authentication**: Required


## Caching

The application uses caching to improve performance by reducing the number of database queries. The following caching strategies are implemented:

- **File Metadata Cache**: Cached list of all file metadata.
  - **Cache Name**: `fileMetadataList`
  - **Cacheable Method**: `getAllFilesMetadata()`

- **File Summary Cache**: Cached summary of all files.
  - **Cache Name**: `fileSummary`
  - **Cacheable Method**: `getSummary()`

## Cache Clearing

The cache is automatically cleared when files are uploaded or deleted to ensure that the cached data remains up-to-date. The `clearCache()` method is used to evict all cached entries:

- **Method**: `clearCache()`
- **Cache Names**: `fileMetadataList`, `fileSummary`

## Configuration

- **File Storage Directory**: The directory where files are stored can be configured in `application.properties` using `file.upload-dir`.

- **Database Configuration**: Set your PostgreSQL database URL, username, and password in `application.properties`.
