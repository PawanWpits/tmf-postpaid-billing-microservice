package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TMF666 Contact sub-resource - Account.contact (0..*).
 * NOTE: the spec's polymorphic list of ContactMedium (phone/email/fax/social/
 * geographic address, each with its own list) is flattened here into a single
 * "primary contact medium" carried directly on the Contact, to avoid a third
 * level of embeddable-collection nesting. {@code mediumType} discriminates
 * which of the fields below is populated ("email", "phone", "fax", "social",
 * "geographicAddress").
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    /** Identifier of the contact within the list of contacts. */
    private String id;

    private String contactName;

    /** e.g. primary, secondary */
    private String contactType;

    /** the party role type linked to the contact, e.g. 'accountManager' */
    private String partyRoleType;

    private Boolean preferred;

    /** "email" | "phone" | "fax" | "social" | "geographicAddress" */
    private String mediumType;

    private String emailAddress;
    private String phoneNumber;
    private String faxNumber;
    private String socialNetworkId;

    private String street1;
    private String street2;
    private String city;
    private String stateOrProvince;
    private String postCode;
    private String country;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDateTime", column = @Column(name = "contact_valid_from")),
            @AttributeOverride(name = "endDateTime", column = @Column(name = "contact_valid_to"))
    })
    private TimePeriod validFor;
}
