package com.sirashop.entity;

public enum RepairStatus {
    RECEIVED,     // Appareil reçu / déposé
    DIAGNOSING,   // En cours de diagnostic
    IN_PROGRESS,  // En cours de réparation
    REPAIRED,     // Réparation terminée (Prêt à être récupéré)
    DELIVERED,    // Récupéré et payé par le client
    CANCELLED     // Annulé / Non réparable
}
