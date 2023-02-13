package portlet.portlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import docs.guestbook.model.Entry;
import docs.guestbook.model.Guestbook;
import docs.guestbook.service.EntryLocalService;
import docs.guestbook.service.GuestbookLocalService;
import org.osgi.service.component.annotations.Reference;
import portlet.constants.GuestbookPortletKeys;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import javax.portlet.*;

import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author nmuchelemba
 */
@Component(
        immediate = true,
        property = {
                "com.liferay.portlet.display-category=category.social",
                "com.liferay.portlet.header-portlet-css=/css/main.css",
                "com.liferay.portlet.scopeable=true",
                "com.liferay.portlet.instanceable=false",
                "javax.portlet.display-name=Guestbook",
                "javax.portlet.init-param.template-path=/",
                "javax.portlet.init-param.view-template=/guestbookwebportlet/view.jsp",
                "javax.portlet.name=" + GuestbookPortletKeys.GUESTBOOK,
                "javax.portlet.resource-bundle=content.Language",
                "javax.portlet.supports.mime-type=text/html",
                "javax.portlet.security-role-ref=power-user,user"
        },
        service = Portlet.class
)
public class GuestbookPortlet extends MVCPortlet {

        public void addEntry(ActionRequest request, ActionResponse response)
                throws PortalException {

                ServiceContext serviceContext = ServiceContextFactory.getInstance(
                        Entry.class.getName(), request);

                String userName = ParamUtil.getString(request, "name");
                String email = ParamUtil.getString(request, "email");
                String message = ParamUtil.getString(request, "message");
                long guestbookId = ParamUtil.getLong(request, "guestbookId");
                long entryId = ParamUtil.getLong(request, "entryId");

                if (entryId > 0) {

                        try {

                                _entryLocalService.updateEntry(
                                        serviceContext.getUserId(), guestbookId, entryId, userName,
                                        email, message, serviceContext);

                                SessionMessages.add(request, "entryAdded");

                                response.setRenderParameter(
                                        "guestbookId", Long.toString(guestbookId));

                        }
                        catch (Exception e) {
                                System.out.println(e);

                                SessionErrors.add(request, e.getClass().getName());

                                PortalUtil.copyRequestParameters(request, response);

                                response.setRenderParameter(
                                        "mvcPath", "/guestbookwebportlet/edit_entry.jsp");
                        }

                }
                else {

                        try {
                                _entryLocalService.addEntry(
                                        serviceContext.getUserId(), guestbookId, userName, email,
                                        message, serviceContext);

                                SessionMessages.add(request, "entryAdded");

                                response.setRenderParameter(
                                        "guestbookId", Long.toString(guestbookId));

                        }
                        catch (Exception e) {
                                SessionErrors.add(request, e.getClass().getName());

                                PortalUtil.copyRequestParameters(request, response);

                                response.setRenderParameter(
                                        "mvcPath", "/guestbookwebportlet/edit_entry.jsp");
                        }
                }
        }


        public void deleteEntry(ActionRequest request, ActionResponse response) throws PortalException {
                long entryId = ParamUtil.getLong(request, "entryId");
                long guestbookId = ParamUtil.getLong(request, "guestbookId");

                ServiceContext serviceContext = ServiceContextFactory.getInstance(
                        Entry.class.getName(), request);

                try {

                        response.setRenderParameter(
                                "guestbookId", Long.toString(guestbookId));

                        _entryLocalService.deleteEntry(entryId, serviceContext);
                }

                catch (Exception e) {
                        Logger.getLogger(GuestbookPortlet.class.getName()).log(
                                Level.SEVERE, null, e);
                }
        }

        @Override
        public void render(RenderRequest renderRequest, RenderResponse renderResponse)
                throws IOException, PortletException {

                try {
                        ServiceContext serviceContext = ServiceContextFactory.getInstance(
                                Guestbook.class.getName(), renderRequest);

                        long groupId = serviceContext.getScopeGroupId();

                        long guestbookId = ParamUtil.getLong(renderRequest, "guestbookId");

                        List<Guestbook> guestbooks = _guestbookLocalService.getGuestbooks(
                                groupId);

                        if (guestbooks.isEmpty()) {
                                Guestbook guestbook = _guestbookLocalService.addGuestbook(
                                        serviceContext.getUserId(), "Main", serviceContext);

                                guestbookId = guestbook.getGuestbookId();
                        }

                        if (guestbookId == 0) {
                                guestbookId = guestbooks.get(0).getGuestbookId();
                        }

                        renderRequest.setAttribute("guestbookId", guestbookId);
                }
                catch (Exception e) {
                        throw new PortletException(e);
                }

                super.render(renderRequest, renderResponse);
        }

        @Reference(unbind = "-")
        protected void setEntryService(EntryLocalService entryLocalService) {
                _entryLocalService = entryLocalService;
        }

        @Reference(unbind = "-")
        protected void setGuestbookService(GuestbookLocalService guestbookLocalService) {
                _guestbookLocalService = guestbookLocalService;
        }

        private EntryLocalService _entryLocalService;
        private GuestbookLocalService _guestbookLocalService;
}