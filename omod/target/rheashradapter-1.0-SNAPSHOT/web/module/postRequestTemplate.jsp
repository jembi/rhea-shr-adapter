<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="template/localHeader.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fx"  %>

<b class="boxHeader"><spring:message code="rheashradapter.postEncounterLogResult"/></b>
<div class="box">
<br />
<table cellpadding="4" cellspacing="0" >
<table>
	<tr style="border:1px solid black;">
		<td style="border:2px solid Aquamarine;"><b><spring:message code="rheashradapter.requestId"/></b></td>
		<td style="border:2px solid Aquamarine;">
			${postEncounterLog.postRequestId}
		</td>
	</tr>
	<c:if test="${not empty postEncounterLog.hl7data}">
	<tr style="border:2px solid Aquamarine;">
		<td style="border:2px solid Aquamarine;"><b><spring:message code="rheashradapter.hl7data"/></b></td>
		<td style="border:2px solid Aquamarine;">
			 ${fx:escapeXml(postEncounterLog.hl7data)}
			<%-- <c:out value="${postEncounterLog.hl7data}"/> --%>
		<%-- <pre>
			${postEncounterLog.hl7data}
		</pre> --%>
		</td>	
	</tr>
	</c:if>
	<c:if test="${not empty postEncounterLog.error}">
		<tr style="border:2px solid Aquamarine;">
		<td style="border:2px solid Aquamarine;"><b><spring:message code="rheashradapter.error"/></b></td>
		<td style="border:2px solid Aquamarine;">
			${postEncounterLog.error}
		</td>
	</tr>
	</c:if>
	
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>