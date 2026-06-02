package com.example.photosorter

interface PhotoDeleter {
    /**
     * Delete the specified photo URIs from the device storage.
     * @param uris List of string URIs to delete.
     * @param onResult Callback invoked with `true` if deletion succeeded (or was approved by user), `false` otherwise.
     */
    fun deletePhotos(uris: List<String>, onResult: (Boolean) -> Unit)
}
