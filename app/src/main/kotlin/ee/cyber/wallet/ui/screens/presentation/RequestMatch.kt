package ee.cyber.wallet.ui.screens.presentation

import ee.cyber.wallet.domain.credentials.CredentialAttribute
import ee.cyber.wallet.domain.credentials.DocType
import ee.cyber.wallet.domain.credentials.Namespace
import ee.cyber.wallet.domain.documents.DocumentField
import eu.europa.ec.eudi.prex.FieldQueryResult
import eu.europa.ec.eudi.prex.Match
import kotlin.collections.asSequence

fun Match.Matched.fields(inputDescriptorId: String, optional: Boolean, docType: DocType): MatchedFields {
    return matches.entries
        .flatMap { matchEntry ->
            matchEntry.value.entries
                .filter { valueEntry -> valueEntry.key == inputDescriptorId }
                .flatMap { valueEntry ->
                    valueEntry.value.matches
                        .asSequence()
                        .filter { it.value is FieldQueryResult.CandidateField.Found }
                        .filter { it.key.optional == optional }
                        .filter { it.key.paths.none { path -> path.value == "\$.type" || path.value == "\$.vct" } } // Still exclude type and vct from results
                        .map {
                            val attr = CredentialAttribute.findByPath(it.key.paths.first().value)
                            MatchedField(
                                field = DocumentField(
                                    namespace = attr?.namespace ?: Namespace.NONE,
                                    name = attr?.fieldName ?: it.key.name?.value ?: it.key.paths.first().value,
                                    value = (it.value as FieldQueryResult.CandidateField.Found).value(),
                                    optional = it.key.optional
                                ),
                                checked = !it.key.optional
                            )
                        }
                }
        }
        .sortedBy { it.order(docType) }
        .toList()
}

fun MatchedField.order(docType: DocType) = CredentialAttribute.find(field.namespace, field.name, docType)?.ordinal ?: Int.MAX_VALUE

fun FieldQueryResult.CandidateField.Found.value() = content.removeSurrounding("\"")
