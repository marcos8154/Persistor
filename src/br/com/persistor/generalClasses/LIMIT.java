/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.persistor.generalClasses;

import br.com.persistor.enums.LIMIT_TYPE;

/**
 *
 * @author marcosvinicius
 */
public class LIMIT
{

    public LIMIT_TYPE limit_type;

    private int pagePosition;
    private int pageSize;
    
    public int getPagePosition()
    {
        return pagePosition;
    }

    public void setPagePosition(int pagePosition)
    {
        this.pagePosition = pagePosition;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public  static LIMIT simpleLimit(int limitSize)
    {
        LIMIT limit = new LIMIT();
        
        limit.limit_type = LIMIT_TYPE.simple;
        limit.setPageSize(limitSize);
        
        return limit;
    }
    
    public static LIMIT paginate(int pagePosition, int pageSize)
    {
        LIMIT limit = new LIMIT();
        
        limit.limit_type = LIMIT_TYPE.paginate;
        limit.setPagePosition(pagePosition);
        limit.setPageSize(pageSize);
        
        return limit;
    }
}
