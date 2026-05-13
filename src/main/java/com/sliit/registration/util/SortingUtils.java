package com.sliit.registration.util;

import com.sliit.registration.model.EnrollmentRequest;
import java.util.List;

/**
 * Utility for Request Auditing explicitly implementing Insertion Sort
 * as required by Member 4 (Arachchi V.A.K.S.V).
 */
public class SortingUtils {

    /**
     * Sorts a list of requests chronologically by timestamp using Insertion Sort.
     * @param requests the list to be sorted
     */
    public static void insertionSortRequests(List<EnrollmentRequest> requests) {
        for (int i = 1; i < requests.size(); i++) {
            EnrollmentRequest key = requests.get(i);
            int j = i - 1;

            // Assuming EnrollmentRequest has a getTimestamp() method returning a long or Date
            // We use getTimestamp() to compare. 
            // Moving elements greater than key to one position ahead
            while (j >= 0 && requests.get(j).getTimestamp() > key.getTimestamp()) {
                requests.set(j + 1, requests.get(j));
                j = j - 1;
            }
            requests.set(j + 1, key);
        }
    }
}
