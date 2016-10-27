package com.vsct.impersonator.http.message;

/**
 * Service permettant de convertir une requête en un identifiant de type String.
 * 
 * Deux requêtes identiques doivent renvoyé le même identifiant et un identifiant ne doit correspondre qu'à une requête.
 */
public interface RequestIdentifier {
	String calculateUid(RawRequest request);
}
