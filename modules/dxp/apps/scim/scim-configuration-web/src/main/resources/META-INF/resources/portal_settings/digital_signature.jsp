<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
	SCIMConfiguration scimConfiguration = (SCIMConfiguration)request.getAttribute(SCIMConfiguration.class.getName());
%>

<div class="row">
	<div class="col-md-12">
		<aui:input checked="<%= scimConfiguration.enabled() %>" inlineLabel="right" label='<%= LanguageUtil.get(resourceBundle, "enabled") %>' labelCssClass="simple-toggle-switch" name="enabled" type="toggle-switch" value="<%= scimConfiguration.enabled() %>" />
	</div>
</div>


<aui:input label="applicationName" name="applicationName" type="text" value="<%= scimConfiguration.applicationName() %>" />


<aui:select label="matcherField" name="matcherField" required="<%= true %>" value="<%= scimConfiguration.matcherField() %>">
	<aui:option label="" value="" />

	<%
		for (String matcherField : SCIMConstants.MATCHER_FIELD) {
	%>

	<aui:option label="<%= matcherField %>" value="<%= matcherField %>" />

	<%
		}
	%>

</aui:select>


<aui:input label="accessToken" name="accessToken" type="text" value="<%= scimConfiguration.applicationName() %>" />



<portlet:actionURL name="/digital_signature/save_site_configuration" var="cancelCheckoutURL">
	<portlet:param name="<%= "cmd" %>" value="cambiar" />
</portlet:actionURL>

<form action="<%= cancelCheckoutURL %>" method="post"  name="fm">
	<liferay-ui:message key="user-account-setup-description" />
	<aui:button
		name="deleteButton"
		id="deleteButton"
		label="discard-changes"
		small="<%= true %>"
		value="send"
	/>
</form>


<script>
	var deleteButtonElement = document.getElementById(
		'<portlet:namespace />deleteButton'
	);

	if (deleteButtonElement) {
		deleteButtonElement.addEventListener('click', (event) => {
			Liferay.Util.openConfirmModal({
				message:
					'<liferay-ui:message key="are-you-sure-you-want-to-delete-this" />',
				onConfirm: (isConfirmed) => {
					if (isConfirmed) {
						var form = window.document['<portlet:namespace />fm'];


						submitForm(form);



					}
				},
			});
		});
	}
</script>