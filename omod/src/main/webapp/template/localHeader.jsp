<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/manageGet") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/rheashradapter/manageGetEncounterLogs.form"><spring:message
				code="rheashradapter.manageGet" /></a>
	</li>
	
	
	<li
		<c:if test='<%= request.getRequestURI().contains("/managePost") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/rheashradapter/managePostEncounterLogs.form"><spring:message
				code="rheashradapter.managePost" /></a>
	</li>
	
	<!-- Add further links here -->
</ul>
