//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

// Replace this using Spring auto-wiring in GeoNetwork core

package org.fao.geonet.kernel.search;

import static org.fao.geonet.constants.Geonet.LUCENE_VERSION;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;

public class ConfigLoader {

	private ServiceContext context;

	ConfigLoader(ServiceContext context) {
		this.context = context;
	}

	public Object newInstance(String className, List<Element> configParams) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName(className);

		ArrayList<Class<?>> argumentClasses = new ArrayList<Class<?>>();
		argumentClasses.addAll(getTypeList(configParams));

		ArrayList<Object> argumentValues = new ArrayList<Object>();
		argumentValues.addAll(getValueList(configParams));

		Constructor<?> c = clazz.getConstructor(argumentClasses.toArray(new Class[0]));
		return c.newInstance(argumentValues.toArray());
	}

	public Class<?>[] getTypes(List<Element> params) throws ClassNotFoundException {
		return getTypeList(params).toArray(new Class[0]);
	}

	private ArrayList<Class<?>> getTypeList(List<Element> params) throws ClassNotFoundException {
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();

		for (Element param : params) {
			String paramType = param.getAttributeValue("type");

			if ("double".equals(paramType)) {
				result.add(double.class);
			} else if ("int".equals(paramType)) {
				result.add(int.class);
			} else {
				result.add(Class.forName(paramType));
			}
		}

		return result;
	}

	public Object[] getValues(List<Element> params) {
		return getValueList(params).toArray();
	}

	private ArrayList<Object> getValueList(List<Element> params) {
		ArrayList<Object> result = new ArrayList<Object>();

		for (Element param : params) {
			String paramType = param.getAttributeValue("type");
			String value = param.getAttributeValue("value");

			if ("org.apache.lucene.util.Version".equals(paramType)) {
				result.add(LUCENE_VERSION);
			} else if (ThesaurusManager.class.getName().equals(paramType)) {
				result.add(getThesaurusManager());
			} else if (ThesaurusFinder.class.getName().equals(paramType)) {
				result.add((ThesaurusFinder) getThesaurusManager());
			}
			else if ("java.io.File".equals(paramType) && value != null) {
				File f = new File(value);
				
				if (!f.exists()) { // try relative to appPath
					f = new File(context.getAppPath() + value);
				}
				if (f != null) {
					result.add(f);
				}
			} else if ("double".equals(paramType) && value != null) {
				result.add(Double.parseDouble(value));
			} else if ("int".equals(paramType) && value != null) {
				result.add(Integer.parseInt(value));
			} else if (value != null) {
				result.add(value);
			} else {
				// No value. eg. Version
			}
		}
		
		return result;
	}

	private ThesaurusManager getThesaurusManager() {
		return getGeonetContext().getThesaurusManager();
	}

	private GeonetContext getGeonetContext() {
		return (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
	}
}
