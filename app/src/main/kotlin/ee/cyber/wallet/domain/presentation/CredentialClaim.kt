package ee.cyber.wallet.domain.presentation

import ee.cyber.wallet.domain.documents.CredentialDocument
import eu.europa.ec.eudi.prex.Claim

interface CredentialClaim : Claim {
    val credentialDocument: CredentialDocument
}
