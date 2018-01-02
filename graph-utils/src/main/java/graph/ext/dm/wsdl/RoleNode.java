package graph.ext.dm.wsdl;

/**
 * Purpose: Represents a role node. Roles are defined for a partnerlink. The consumer typically
 * conforms to myRole, while the partner can implement the partnerRole. The role connects
 * the partnerlink to the port (aka interface).
 * 
 * @author mwa17610
 * 
 */
public class RoleNode extends WSDLNode {
    public RoleNode() {
        super();
    }
}
