package uz.fido.pfexchange.service;

public interface MinyustFamilyBatchRequestProcessor {

    void processAllPendingRequests();
    long getPendingCount();
}
