<%@ page import="com.yjfei.pgateway.common.FilterInfo"%>
<%@ page import="com.yjfei.pgateway.common.IGateFilterDao"%>
<%@ page import="com.yjfei.pgateway.filters.GateFilterDaoFactory"%>
<%@ page import="com.yjfei.pgateway.util.AdminFilterUtil"%>
<%@ page import="org.slf4j.Logger"%>
<%@ page import="org.slf4j.LoggerFactory"%>
<%@ page import="java.util.List"%>


<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%
    Logger LOG = LoggerFactory.getLogger("filterloader");

    IGateFilterDao gateFilterDao = null;
    try {
    	gateFilterDao = GateFilterDaoFactory.getGateFilterDao();
    } catch (Exception e) {
        LOG.error(e.getMessage(), e);
    }
    List<String> filterIds = gateFilterDao.getAllFilterIds();
%>
<html>
<head>
<title></title>
</head>
<body>

	UPLOAD
	<form method="POST" enctype="multipart/form-data"
		action="scriptmanager?action=UPLOAD">
		<input type="file" name="upload" size="40" /> <input type="submit" />
	</form>

	<br> ACTIVE SCRIPTS
	<table border="1">
		<tr>
			<td>NAME</td>
			<td>TYPE</td>
			<td>ORDER</td>
			<td>DISABLE PROPERTY</td>
			<td>CREATE DATE</td>
			<td>REVISION</td>
			<td>STATE</td>
		</tr>
		<%

        List<FilterInfo> filters = gateFilterDao.getAllActiveFilters();
        for (FilterInfo filter : filters) {
    %>
		<tr>
			<td><%=filter.getFilterName()%></td>
			<td><%=filter.getFilterType()%></td>
			<td><%=filter.getFilterOrder()%></td>
			<td><%=filter.getFilterDisablePropertyName()%></td>
			<td><%=filter.getCreateTime()%></td>
			<td><%=filter.getRevision()%></td>
			<td><%=AdminFilterUtil.getState(filter)%></td>
			<td><%=AdminFilterUtil.buildDeactivateForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildDownloadLink(filter.getFilterId(), filter.getRevision())%>
			</td>
		</tr>
		<%
        }
    %>
	</table>


	<br> CANARY SCRIPTS
	<table border="1">
		<tr>
			<td>NAME</td>
			<td>TYPE</td>
			<td>ORDER</td>
			<td>DISABLE PROPERTY</td>
			<td>CREATE DATE</td>
			<td>REVISION</td>
			<td>STATE</td>
		</tr>
		<%

        filters = gateFilterDao.getAllCanaryFilters();
        for (FilterInfo filter : filters) {
    %>
		<tr>
			<td><%=filter.getFilterName()%></td>
			<td><%=filter.getFilterType()%></td>
			<td><%=filter.getFilterOrder()%></td>
			<td><%=filter.getFilterDisablePropertyName()%></td>
			<td><%=filter.getCreateTime()%></td>
			<td><%=filter.getRevision()%></td>
			<td><%=AdminFilterUtil.getState(filter)%></td>
			<td><%=AdminFilterUtil.buildDeactivateForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildActivateForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildDownloadLink(filter.getFilterId(), filter.getRevision())%>
			</td>
		</tr>
		<%
        }
    %>
	</table>

	<br> LATEST SCRIPTS
	<table border="1">
		<tr>
			<td>NAME</td>
			<td>TYPE</td>
			<td>ORDER</td>
			<td>DISABLE PROPERTY</td>
			<td>CREATE DATE</td>
			<td>REVISION</td>
			<td>STATE</td>
			<td>Activate</td>
			<td>Make Canary</td>

		</tr>
		<%

        for (String filterID : filterIds) {
            FilterInfo filter = gateFilterDao.getLatestFilter(filterID);
    %>
		<tr>
			<td><%=filter.getFilterName()%></td>
			<td><%=filter.getFilterType()%></td>
			<td><%=filter.getFilterOrder()%></td>
			<td><%=filter.getFilterDisablePropertyName()%></td>
			<td><%=filter.getCreateTime()%></td>
			<td><%=filter.getRevision()%></td>
			<td><%=AdminFilterUtil.getState(filter)%></td>
			<td><%=AdminFilterUtil.buildActivateForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildCanaryForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildDownloadLink(filter.getFilterId(), filter.getRevision())%>
			</td>
		</tr>
		<%
        }
    %>
	</table>


	<br> All SCRIPTS
	<table border="1">
		<tr>
			<td>NAME</td>
			<td>TYPE</td>
			<td>ORDER</td>
			<td>DISABLE PROPERTY</td>
			<td>CREATE DATE</td>
			<td>REVISION</td>
			<td>STATE</td>
			<td>Activate</td>
			<td>Make Canary</td>
		</tr>
		<%

        for (String filterID : filterIds) {
            filters = gateFilterDao.getGateFilters(filterID);
            for (FilterInfo filter : filters) {
    %>
		<tr>
			<td><%=filter.getFilterName()%></td>
			<td><%=filter.getFilterType()%></td>
			<td><%=filter.getFilterOrder()%></td>
			<td><%=filter.getFilterDisablePropertyName()%></td>
			<td><%=filter.getCreateTime()%></td>
			<td><%=filter.getRevision()%></td>
			<td><%=AdminFilterUtil.getState(filter)%></td>
			<td><%=AdminFilterUtil.buildActivateForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildCanaryForm(filter.getFilterId(), filter.getRevision())%>
			</td>
			<td><%=AdminFilterUtil.buildDownloadLink(filter.getFilterId(), filter.getRevision())%>
			</td>
		</tr>
		<%
            }
        }
    %>

	</table>

</body>
</html>