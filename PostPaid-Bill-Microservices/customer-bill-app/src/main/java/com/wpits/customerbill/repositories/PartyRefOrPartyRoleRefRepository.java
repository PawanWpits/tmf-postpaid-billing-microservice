package com.wpits.customerbill.repositories;

import com.wpits.customerbill.entities.PartyRefOrPartyRoleRef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRefOrPartyRoleRefRepository extends JpaRepository<PartyRefOrPartyRoleRef, String> {
    java.util.Optional<PartyRefOrPartyRoleRef> findFirstByBillingAccountIdAndPartyId(
            String billingAccountId, String partyId);
}
