package com.autoservice.service;

import com.autoservice.domain.PartRequest;
import com.autoservice.domain.SparePart;

import java.util.*;

public class Inventory {
    private final Map<String, SparePart> parts = new LinkedHashMap<>();
    private final Map<String, Integer> stock = new HashMap<>();
    private final List<PartRequest> partRequests = new ArrayList<>();

    /** Mechanik vytvorí požiadavku – bez ceny */
    public PartRequest createRequest(String orderId, String mechanicId, String partName, int quantity) {
        PartRequest req = new PartRequest(orderId, mechanicId, partName, quantity);
        partRequests.add(req);
        return req;
    }

    /** Skladník doplní cenu a označí ako objednané */
    public void fulfillRequest(String requestId, double price) {
        partRequests.stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst()
                .ifPresent(r -> r.markOrdered(price));
    }

    /** Skladník označí diel ako doručený */
    public void deliverRequest(String requestId) {
        partRequests.stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst()
                .ifPresent(PartRequest::markDelivered);
    }

    public List<PartRequest> getPendingRequests() {
        return partRequests.stream()
                .filter(r -> r.getStatus() == PartRequest.Status.PENDING)
                .toList();
    }

    public List<PartRequest> getAllRequests() {
        return Collections.unmodifiableList(partRequests);
    }

    public List<PartRequest> getRequestsForOrder(String orderId) {
        return partRequests.stream()
                .filter(r -> r.getOrderId().equals(orderId))
                .toList();
    }

    public boolean confirmDelivery(String partId) {
        SparePart part = parts.get(partId);
        if (part != null) {
            stock.merge(partId, 1, Integer::sum);
            return true;
        }
        return false;
    }

    public boolean confirmParts(String partId) {
        return stock.getOrDefault(partId, 0) > 0;
    }

    public void flagInboundPart(String partId) {
        System.out.println("Diel " + partId + " označený ako objednávaný");
    }

    public void markForOrder(String partId, int quantity) {
        System.out.println("Objednávam " + quantity + "x diel " + partId);
    }

    public boolean orderPart(String partId, String orderId, int qty) {
        markForOrder(partId, qty);
        return true;
    }

    public List<SparePart> reserveParts(List<SparePart> requestedParts) {
        List<SparePart> reserved = new ArrayList<>();
        for (SparePart part : requestedParts) {
            int available = stock.getOrDefault(part.getPartId(), 0);
            int needed = 1;
            try { needed = Integer.parseInt(part.getQuantity()); } catch (Exception ignored) {}
            if (available >= needed) {
                stock.put(part.getPartId(), available - needed);
                reserved.add(part);
            }
        }
        return reserved;
    }

    public boolean isAvailable(String partId, int qty) {
        return stock.getOrDefault(partId, 0) >= qty;
    }

    public void addPart(SparePart part, int quantity) {
        parts.put(part.getPartId(), part);
        stock.put(part.getPartId(), stock.getOrDefault(part.getPartId(), 0) + quantity);
    }

    public List<SparePart> getAllParts() {
        return new ArrayList<>(parts.values());
    }

    public int getStock(String partId) {
        return stock.getOrDefault(partId, 0);
    }
}