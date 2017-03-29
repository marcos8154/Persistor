/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.enums;

/**
 *
 * @author Marcos Vinícius
 */
public enum ISOLATION_LEVEL
{
    TRANSACTION_NONE,
    TRANSACTION_READ_COMMITTED,
    TRANSACTION_READ_UNCOMMITTED,
    TRANSACTION_REPEATABLE_READ,
    TRANSACTION_SERIALIZABLE
}
