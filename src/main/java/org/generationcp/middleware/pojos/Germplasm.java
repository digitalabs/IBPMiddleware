/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.middleware.pojos;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.generationcp.middleware.dao.GermplasmListDAO;
import org.generationcp.middleware.domain.inventory.GermplasmInventory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * POJO for germplsm table.
 *
 * @author Kevin Manansala, Mark Agarrado, Dennis Billano
 */
@NamedQueries({
	@NamedQuery(name = "getAllGermplasm", query = "FROM Germplasm"),
	@NamedQuery(name = "countAllGermplasm", query = "SELECT COUNT(g) FROM Germplasm g"),

	@NamedQuery(name = "countMatchGermplasmInList", query = "SELECT COUNT(g) FROM Germplasm g WHERE g.gid IN (:gids)"),

	@NamedQuery(name = "getGermplasmByMethodNameUsingEqual",
		query = "SELECT g FROM Germplasm g, Method m WHERE g.methodId = m.mid AND m.mname = :name"),

	@NamedQuery(name = "countGermplasmByMethodNameUsingEqual",
		query = "SELECT COUNT(g) FROM Germplasm g, Method m WHERE g.methodId = m.mid AND m.mname = :name"),

	@NamedQuery(name = "getGermplasmByMethodNameUsingLike",
		query = "SELECT g FROM Germplasm g, Method m WHERE g.methodId = m.mid AND m.mname like :name"),

	@NamedQuery(name = "countGermplasmByMethodNameUsingLike",
		query = "SELECT COUNT(g) FROM Germplasm g, Method m WHERE g.methodId = m.mid AND m.mname like :name"),

	@NamedQuery(name = "getGermplasmByLocationNameUsingEqual",
		query = "SELECT g FROM Germplasm g, Location l WHERE g.locationId = l.locid AND l.lname = :name"),

	@NamedQuery(name = "countGermplasmByLocationNameUsingEqual",
		query = "SELECT COUNT(g) FROM Germplasm g, Location l WHERE g.locationId = l.locid AND l.lname = :name"),

	@NamedQuery(name = "getGermplasmByLocationNameUsingLike",
		query = "SELECT g FROM Germplasm g, Location l WHERE g.locationId = l.locid AND l.lname like :name"),

	@NamedQuery(name = "countGermplasmByLocationNameUsingLike",
		query = "SELECT COUNT(g) FROM Germplasm g, Location l WHERE g.locationId = l.locid AND l.lname like :name")

})
@NamedNativeQueries({
	@NamedNativeQuery(name = "getGermplasmDescendants",
		query = "SELECT DISTINCT g.* FROM germplsm g LEFT JOIN progntrs p ON g.gid = p.gid "
			+ "WHERE (g.gpid1=:gid OR g.gpid2=:gid OR p.pid=:gid) " + "AND  g.deleted = 0  and g.grplce = 0",
		resultClass = Germplasm.class),

	@NamedNativeQuery(name = "getGermplasmByPrefName",
		query = "SELECT g.* FROM germplsm g LEFT JOIN names n ON g.gid = n.gid " + "AND n.nstat = 1 " + "WHERE n.nval = :name",
		resultClass = Germplasm.class),

	@NamedNativeQuery(name = "getProgenitor1",
		query = "SELECT p.* FROM germplsm g, germplsm p WHERE g.gid = :gid "
			+ "and g.gpid1 = p.gid and p.deleted = 0 and p.grplce = 0",
		resultClass = Germplasm.class),

	@NamedNativeQuery(name = "getProgenitor2",
		query = "SELECT p.* FROM germplsm g, germplsm p WHERE g.gid = :gid "
			+ "and g.gpid2 = p.gid and p.deleted = 0 and p.grplce = 0",
		resultClass = Germplasm.class),

	@NamedNativeQuery(name = "getProgenitor", query = "SELECT g.* FROM germplsm g, progntrs p WHERE g.gid = p.pid "
		+ "and p.gid = :gid and p.pno = :pno and  g.deleted = 0  and g.grplce = 0", resultClass = Germplasm.class)})
@Entity
@Table(name = "germplsm")
// JAXB Element Tags for JSON output
@XmlRootElement(name = "germplasm")
@XmlType(propOrder = {"gid", "gnpgs", "gpid1", "gpid2", "gdate"})
@XmlAccessorType(XmlAccessType.NONE)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "germplsm")
public class Germplasm extends AbstractEntity implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	// string contants for name of queries
	public static final String GET_ALL = "getAllGermplasm";
	public static final String COUNT_ALL = "countAllGermplasm";
	public static final String COUNT_MATCH_GERMPLASM_IN_LIST = "countMatchGermplasmInList";
	public static final String GET_BY_PREF_NAME = "getGermplasmByPrefName";
	public static final String COUNT_BY_PREF_NAME =
		"SELECT COUNT(g.gid) " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 " + "WHERE n.nval = :name";
	public static final String GET_BY_METHOD_NAME_USING_EQUAL = "getGermplasmByMethodNameUsingEqual";
	public static final String COUNT_BY_METHOD_NAME_USING_EQUAL = "countGermplasmByMethodNameUsingEqual";
	public static final String GET_BY_METHOD_NAME_USING_LIKE = "getGermplasmByMethodNameUsingLike";
	public static final String COUNT_BY_METHOD_NAME_USING_LIKE = "countGermplasmByMethodNameUsingLike";
	public static final String GET_BY_LOCATION_NAME_USING_EQUAL = "getGermplasmByLocationNameUsingEqual";
	public static final String COUNT_BY_LOCATION_NAME_USING_EQUAL = "countGermplasmByLocationNameUsingEqual";
	public static final String GET_BY_LOCATION_NAME_USING_LIKE = "getGermplasmByLocationNameUsingLike";
	public static final String COUNT_BY_LOCATION_NAME_USING_LIKE = "countGermplasmByLocationNameUsingLike";

	public static final String GET_BY_GID_WITH_PREF_NAME =
		"SELECT {g.*}, {n.*} " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 " + "WHERE g.gid = :gid";

	public static final String GET_BY_GID_WITH_PREF_ABBREV =
		"SELECT {g.*}, {n.*}, {abbrev.*} " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 "
			+ "LEFT JOIN names abbrev ON g.gid = abbrev.gid AND abbrev.nstat = 2 " + "WHERE g.gid = :gid";
	public static final String GET_DESCENDANTS = "getGermplasmDescendants";

	public static final String COUNT_DESCENDANTS =
		"SELECT COUNT(DISTINCT g.gid) " + "FROM germplsm g LEFT JOIN progntrs p ON g.gid = p.gid "
			+ "WHERE (g.gpid1 = :gid OR g.gpid2 = :gid OR p.pid=:gid) " + "AND  g.deleted = 0  and g.grplce = 0";
	public static final String GET_PROGENITOR1 = "getProgenitor1";
	public static final String GET_PROGENITOR2 = "getProgenitor2";
	public static final String GET_PROGENITOR = "getProgenitor";

	public static final String GET_PROGENITORS_BY_GIDS_WITH_PREF_NAME =
		"SELECT p.gid, {g.*}, {n.*}, (select pMale.grpName from listdata pMale where pMale.gid = g.gid limit 1) as malePedigree "
			+ "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 "
			+ "JOIN progntrs p ON p.pid = g.gid "
			+ "WHERE p.gid in (:gidList) and  g.deleted = 0  and g.grplce = 0 "
			+ "ORDER BY p.gid, p.pno";

	public static final String GET_MANAGEMENT_NEIGHBORS =
		"SELECT {g.*}, {n.*} " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 "
			+ "WHERE g.mgid = :gid AND  g.deleted = 0  and g.grplce = 0 ORDER BY g.gid";

	public static final String COUNT_MANAGEMENT_NEIGHBORS =
		"SELECT COUNT(g.gid) " + "FROM germplsm g " + "WHERE g.mgid = :gid AND  g.deleted = 0  and g.grplce = 0";
	public static final String GET_GROUP_RELATIVES =
		"SELECT {g.*}, {n.*} " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 "
			+ "JOIN germplsm g2 ON g.gpid1 = g2.gpid1 " + "WHERE g.gnpgs = -1 AND g.gid <> :gid AND g2.gid = :gid "
			+ "AND g.gpid1 != 0 AND  g.deleted = 0  AND g.grplce = 0";
	public static final String COUNT_GROUP_RELATIVES =
		"SELECT COUNT(g.gid) " + "FROM germplsm g " + "JOIN germplsm g2 ON g.gpid1 = g2.gpid1 "
			+ "WHERE g.gnpgs = -1 AND g.gid <> :gid AND g2.gid = :gid " + "AND g.gpid1 != 0 AND  g.deleted = 0  AND g.grplce = 0";

	public static final String GET_DERIVATIVE_CHILDREN =
		"SELECT {g.*}, {n.*} " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 "
			+ "WHERE g.gnpgs = -1 AND g.gpid2 = :gid and  g.deleted = 0  and g.grplce = 0";

	public static final String GET_MAINTENANCE_CHILDREN =
		"SELECT {g.*}, {n.*} " + "FROM germplsm g LEFT JOIN names n ON g.gid = n.gid AND n.nstat = 1 "
			+ "JOIN methods m ON g.methn = m.mid AND m.mtype = 'MAN' "
			+ "WHERE g.gnpgs = -1 AND g.gpid2 = :gid and  g.deleted = 0  and g.grplce = 0";

	public static final String GET_BY_NAME_ALL_MODES_USING_EQUAL =
		"SELECT DISTINCT {g.*} FROM germplsm g JOIN names n ON g.gid = n.gid WHERE  g.deleted = 0  AND g.grplce = 0 AND "
			+ "( nval = :name OR nval = :noSpaceName OR nval = :standardizedName )";

	public static final String COUNT_BY_NAME_ALL_MODES_USING_EQUAL =
		"SELECT COUNT(DISTINCT g.gid) FROM germplsm g JOIN names n ON g.gid = n.gid and n.nstat != 9 WHERE  g.deleted = 0  AND g.grplce = 0 AND "
			+ "( nval = :name OR nval = :noSpaceName OR nval = :standardizedName )";

	public static final String GET_BY_NAME_ALL_MODES_USING_LIKE =
		"SELECT DISTINCT {g.*} FROM germplsm g JOIN names n ON g.gid = n.gid WHERE  g.deleted = 0  AND g.grplce = 0 AND "
			+ "( nval LIKE :name OR nval LIKE :noSpaceName OR nval LIKE :standardizedName )";

	public static final String COUNT_BY_NAME_ALL_MODES_USING_LIKE =
		"SELECT COUNT(DISTINCT g.gid) FROM germplsm g JOIN names n ON g.gid = n.gid and n.nstat != 9 WHERE  g.deleted = 0  AND g.grplce = 0 AND "
			+ "( nval LIKE :name OR nval LIKE :noSpaceName OR nval LIKE :standardizedName )";

	/**
	 * Used in germplasm data manager searchForGermplasm
	 */
	public static final String GENERAL_SELECT_FROM = "SELECT * FROM ";
	public static final String GERMPLASM_ALIAS = "AS germplasm ";
	public static final String INVENTORY_ALIAS = "AS inventory ";
	public static final String JOIN_ON_GERMPLASM_AND_INVENTORY = "ON germplasm.gid = inventory.entity_id ";
	public static final String SEARCH_GERMPLASM_WITH_INVENTORY =
		"SELECT entity_id, CAST(SUM(CASE WHEN avail_bal = 0 THEN 0 ELSE 1 END) AS UNSIGNED) as availInv, Count(DISTINCT lotid) as seedRes "
			+ "FROM ( SELECT i.lotid, i.eid AS entity_id, " + "SUM(trnqty) AS avail_bal " + "FROM ims_lot i "
			+ "LEFT JOIN ims_transaction act ON act.lotid = i.lotid AND act.trnstat <> 9 "
			+ "WHERE i.status = 0 AND i.etype = 'GERMPLSM' " + "GROUP BY i.lotid " + "HAVING avail_bal > -1) inv "
			+ "GROUP BY entity_id";

	public static final String GET_GERMPLASM_DATES_BY_GIDS = "SELECT gid, gdate " + "FROM germplsm " + "WHERE gid IN (:gids)";
	public static final String GET_METHOD_IDS_BY_GIDS = "SELECT gid, methn " + "FROM germplsm " + "WHERE gid IN (:gids)";
	public static final String GET_PARENT_NAMES_BY_STUDY_ID =
		"select n.gid, n.ntype, n.nval, n.nid, n.nstat  \n" +
			"from stock s\n" +
			"inner join germplsm g on g.gid = s.dbxref_id\n" +
			"inner join names n on (n.gid = g.gpid1 or n.gid = g.gpid2)\n" +
			"where s.project_id = :projId";

	public static final String GET_KNOWN_PARENT_GIDS_BY_STUDY_ID =
		"select g.gid, g.gpid1, g.gpid2, g.grplce \n" +
			"from stock s\n" +
			"inner join germplsm g on g.gid = s.dbxref_id and g.gnpgs > 0 AND (g.gpid1 > 0 or g.gpid2 > 0)   \n" +
			"where s.project_id = :projId";

	public static final String GET_PREFERRED_NAME_AND_PARENT_FOR_A_GID_LIST = "select g.gid as gid, ld.grpname as pedigree, "
		+ " (select n.nval from names n where n.nstat=1 and n.gid = g.gid limit 1) as nval" + "  from germplsm g "
		+ " inner join listdata ld on (g.gid = ld.gid) " + "where g.gid in (:gidList) " + "group by g.gid";

	public static final String GET_GERMPLASM_OFFSPRING_BY_GID =
		" SELECT DISTINCT \n" + "   g.gid, \n" + "   CONCAT_WS(',', \n" + "     if(g.gpid1 != 0, g.gpid1, NULL), \n"
			+ "     if(g.gpid2 != 0, g.gpid2, NULL), \n" + "     ifnull(p.pid, NULL)) AS parents \n" + " FROM germplsm g \n"
			+ "   LEFT JOIN progntrs p ON g.gid = p.gid \n" + "   LEFT JOIN listdata ld ON g.gid = ld.gid \n"
			+ "   LEFT JOIN listnms l ON ld.listid = l.listid \n"
			+ " WHERE (g.gpid1 in (:gids) OR g.gpid2 in (:gids) OR p.pid in (:gids)) \n" + "   AND g.deleted = 0 \n"
			+ "   AND g.grplce = 0 \n" + "   AND ( l.liststatus != " + GermplasmListDAO.STATUS_DELETED
			+ " OR l.liststatus IS NULL)";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "gid")
	@XmlElement(name = "gid")
	private Integer gid;

	@Basic(optional = false)
	@Column(name = "methn")
	private Integer methodId;

	@Basic(optional = false)
	@Column(name = "gnpgs")
	@XmlElement(name = "numberOfProgenitors")
	private Integer gnpgs;

	/**
	 * Usually female parent.
	 */
	@Basic(optional = false)
	@Column(name = "gpid1")
	@XmlElement(name = "firstParent")
	private Integer gpid1;

	/**
	 * Usually male parent.
	 */
	@Basic(optional = false)
	@Column(name = "gpid2")
	@XmlElement(name = "secondParent")
	private Integer gpid2;

	@Basic(optional = false)
	@Column(name = "lgid")
	private Integer lgid;

	@Basic(optional = false)
	@Column(name = "glocn")
	private Integer locationId;

	@Basic(optional = false)
	@Column(name = "gdate")
	@XmlElement(name = "creationDate")
	private Integer gdate;

	@Basic(optional = false)
	@Column(name = "gref")
	private Integer referenceId;

	/**
	 * Records deletion or replacement for the current record. 0=unchanged, own GID=deleted, replacement GID=replaced
	 */
	@Basic(optional = false)
	@Column(name = "grplce")
	private Integer grplce;

	/*
	 * If the current germplasm is a managed sample then MGID contains the GID of the germplasm at the root of the management tree, else 0.
	 * This the GROUP_ID of the germplasm.
	 */
	@Basic(optional = false)
	@Column(name = "mgid")
	private Integer mgid;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "gid")
	private List<Name> names = new ArrayList<Name>();

	@Type(type = "org.hibernate.type.NumericBooleanType")
	@Basic(optional = false)
	@Column(name = "deleted", columnDefinition = "TINYINT")
	private Boolean deleted;

	@Column(name = "germplsm_uuid")
	private String germplasmUUID;

	@OneToOne(fetch = FetchType.LAZY)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "gref", insertable = false, updatable = false)
	private Bibref bibref;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "methn", insertable = false, updatable = false)
	private Method method;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "gid")
	private List<ExternalReference> externalReferences = new ArrayList<>();

	@OneToMany(mappedBy = "germplasm", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Progenitor> otherProgenitors = new ArrayList<>();

	/**
	 * This variable is populated only when the Germplasm POJO is retrieved by using GermplasmDataManager.getGermplasmWithPrefName() and
	 * GermplasmDataManager.getGermplasmWithPrefAbbrev(). Otherwise it is null always.
	 */
	@Transient
	private Name preferredName = null;

	/**
	 * This variable is populated only when the Germplasm POJO is retrieved by using GermplasmDataManager.getGermplasmWithPrefAbbrev().
	 * Otherwise it is null always.
	 */
	@Transient
	private String preferredAbbreviation = null;

	/**
	 * This variable is populated only when the Germplasm POJO is retrieved by using GermplasmDataManager.searchForGermplasm(). Otherwise it
	 * is null always.
	 */
	@Transient
	private GermplasmInventory inventoryInfo;

	/**
	 * This variable is populated only when the Germplasm POJO is retrieved by using GermplasmDataManager.getDirectParentsForStudy().
	 * Otherwise it is null always.
	 */
	@Transient
	private String selectionHistory = null;

	/**
	 * This variable is populated only when the Germplasm POJO is retrieved by using GermplasmDataManager.getDirectParentsForStudy().
	 * Otherwise it is null always.
	 */
	@Transient
	private String crossName = null;

	/**
	 * This variable is populated only when the Germplasm POJO is retrieved by using GermplasmDataManager.getDirectParentsForStudy().
	 * Otherwise it is null always.
	 */
	@Transient
	private String accessionName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 * Previously, germplasm list is loaded and revisit the DB for each germplasm for getting method name.
	 * This problem is removed by introducing this variable.
	 */
	@Transient
	private String methodName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 * Previously, germplasm list is loaded and revisit the DB for each germplasm for getting location name.
	 * This problem is removed by introducing this variable.
	 */
	@Transient
	private String locationName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String methodNumber = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String methodGroup = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String methodCode = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String germplasmPeferredName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String germplasmPeferredId = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String femaleParentPreferredName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String femaleParentPreferredID = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String maleParentPreferredName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String maleParentPreferredID = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String germplasmNamesString = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String germplasmDate = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private Map<String, String> attributeTypesValueMap = new HashMap<>();

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private Map<String, String> nameTypesValueMap = new HashMap<>();

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String groupSourcePreferredName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String groupSourceGID = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String immediateSourcePreferredName = null;

	/**
	 * This variable is populated when the user tries to search germplasm list.
	 */
	@Transient
	private String immediateSourceGID = null;

	/**
	 * Don't use it. This constructor is required by hibernate.
	 */
	public Germplasm() {
		this.deleted = false;
	}

	public Germplasm(final Integer gid, final Integer methodId, final Integer gnpgs, final Integer gpid1, final Integer gpid2,
		final Integer lgid, final Integer locationId, final Integer gdate, final Integer referenceId,
		final Integer grplce, final Integer mgid, final Name preferredName, final String preferredAbbreviation, final Method method) {
		this(gid);
		this.gid = gid;
		this.methodId = methodId;
		this.gnpgs = gnpgs;
		this.gpid1 = gpid1;
		this.gpid2 = gpid2;
		this.lgid = lgid;
		this.locationId = locationId;
		this.gdate = gdate;
		this.referenceId = referenceId;
		this.grplce = grplce;
		this.mgid = mgid;
		this.preferredName = preferredName;
		this.preferredAbbreviation = preferredAbbreviation;
		this.method = method;
		this.deleted = false;
	}

	public Germplasm(final Integer gid, final Integer methodId, final Integer gnpgs, final Integer gpid1, final Integer gpid2,
		final Integer lgid, final Integer locationId, final Integer gdate, final Name preferredName) {

		// gref =0, grplce = 0, mgid = 0
		this(gid, methodId, gnpgs, gpid1, gpid2, lgid, locationId, gdate, 0, 0, 0, preferredName, null, null);
	}

	//TODO: cleanup - remove it.
	@Deprecated
	public Germplasm(final Integer gid) {
		this.gid = gid;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	/**
	 * Represents the type of genesis and number of progenitors.
	 *
	 * <ul>
	 * <li>For a derivative process GNPGS = -1 and then GPID1 contains the germplasm groupID and GPID2 the source germplasm ID.</li>
	 *
	 * <li>For a generative process GNPGS containsthe number of specified parents. (The number of parents required by a method isrecorded by
	 * NPRGN on the METHODS TABLE). If GNPGS = 1 or 2 then the IDs ofthe progenitors are contained in the GPID1 and GPID2 fields on the
	 * GERMPLSM table. If GNPGS>2 then further IDs are stored on the PROGNTRS table.</li>
	 *
	 * <li>GNPGS = 0 for <a href="https://en.wikipedia.org/wiki/Landrace">landrace</a> or wild species collections or if none of the parents
	 * is known.GNPGS <= NPRGN, but some of the GNPGS specified parents may be unknown inwhich case the corresponding GPIDs are MISSING (0).
	 * For example in a simplecross with only male parent known, GNPGS would have to be 2 with GPID1 = 0 and GPID2 set to GID of the known
	 * male parent.</li>
	 * </ul>
	 */
	public Integer getGnpgs() {
		return this.gnpgs;
	}

	public void setGnpgs(final Integer gnpgs) {
		this.gnpgs = gnpgs;
	}

	public Integer getGpid1() {
		return this.gpid1;
	}

	public void setGpid1(final Integer gpid1) {
		this.gpid1 = gpid1;
	}

	public Integer getGpid2() {
		return this.gpid2;
	}

	public void setGpid2(final Integer gpid2) {
		this.gpid2 = gpid2;
	}

	public Integer getLgid() {
		return this.lgid;
	}

	public void setLgid(final Integer lgid) {
		this.lgid = lgid;
	}

	public Integer getGdate() {
		return this.gdate;
	}

	public void setGdate(final Integer gdate) {
		this.gdate = gdate;
	}

	public Integer getGrplce() {
		return this.grplce;
	}

	public void setGrplce(final Integer grplce) {
		this.grplce = grplce;
	}

	public Integer getMgid() {
		return this.mgid;
	}

	public void setMgid(final Integer mgid) {
		this.mgid = mgid;
	}

	public Integer getMethodId() {
		return this.methodId;
	}

	public void setMethodId(final Integer methodId) {
		this.methodId = methodId;
	}

	public Integer getLocationId() {
		return this.locationId;
	}

	public void setLocationId(final Integer locationId) {
		this.locationId = locationId;
	}

	public Integer getReferenceId() {
		return this.referenceId;
	}

	public void setReferenceId(final Integer referenceId) {
		this.referenceId = referenceId;
	}

	public Name getPreferredName() {
		if (this.preferredName == null) {
			for (final Name name : this.getNames()) {
				if (name.getNstat() != null && name.getNstat().equals(new Integer(1))) {
					this.preferredName = name;
				}
			}
		}
		return this.preferredName;
	}

	public void setPreferredName(final Name preferredName) {
		this.preferredName = preferredName;
	}

	public String getPreferredAbbreviation() {
		return this.preferredAbbreviation;
	}

	public void setPreferredAbbreviation(final String preferredAbbreviation) {
		this.preferredAbbreviation = preferredAbbreviation;
	}

	public void setMethod(final Method method) {
		this.method = method;
	}

	public Method getMethod() {
		return this.method;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(final String locationName) {
		this.locationName = locationName;
	}

	public List<ExternalReference> getExternalReferences() {
		return externalReferences;
	}

	public void setExternalReferences(final List<ExternalReference> externalReferences) {
		this.externalReferences = externalReferences;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Germplasm)) {
			return false;
		}

		final Germplasm rhs = (Germplasm) obj;
		return new EqualsBuilder().append(this.gid, rhs.gid).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this.gid).toHashCode();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Germplasm [gid=");
		builder.append(this.gid);
		builder.append(", methodId=");
		builder.append(this.methodId);
		builder.append(", gnpgs=");
		builder.append(this.gnpgs);
		builder.append(", gpid1=");
		builder.append(this.gpid1);
		builder.append(", gpid2=");
		builder.append(this.gpid2);
		builder.append(", createdBy=");
		builder.append(super.getCreatedBy());
		builder.append(", lgid=");
		builder.append(this.lgid);
		builder.append(", locationId=");
		builder.append(this.locationId);
		builder.append(", gdate=");
		builder.append(this.gdate);
		builder.append(", referenceId=");
		builder.append(this.referenceId);
		builder.append(", grplce=");
		builder.append(this.grplce);
		builder.append(", mgid=");
		builder.append(this.mgid);
		builder.append(", preferredName=");
		builder.append(this.preferredName);
		builder.append(", preferredAbbreviation=");
		builder.append(this.preferredAbbreviation);
		builder.append(", method=");
		builder.append(this.method);
		builder.append(", inventoryInfo=");
		builder.append(this.inventoryInfo);
		builder.append(", methodName=");
		builder.append(this.methodName);
		builder.append(", locationName=");
		builder.append(this.locationName);
		builder.append("]");
		return builder.toString();
	}

	public GermplasmInventory getInventoryInfo() {
		return this.inventoryInfo;
	}

	public void setInventoryInfo(final GermplasmInventory inventoryInfo) {
		this.inventoryInfo = inventoryInfo;
	}

	public String getSelectionHistory() {
		return this.selectionHistory;
	}

	public void setSelectionHistory(final String selectionHistory) {
		this.selectionHistory = selectionHistory;
	}

	public String getCrossName() {
		return this.crossName;
	}

	public void setCrossName(final String crossName) {
		this.crossName = crossName;
	}

	public String getAccessionName() {
		return this.accessionName;
	}

	public void setAccessionName(final String accessionName) {
		this.accessionName = accessionName;
	}

	/**
	 * @return <strong>ALL</strong> name records associated with this germplasm entity.
	 */
	public List<Name> getNames() {
		return this.names;
	}

	public void setNames(final List<Name> names) {
		this.names = names;
	}

	public Name findPreferredName() {
		Name foundPreferredName = null;
		for (final Name name : this.getNames()) {
			if (new Integer(1).equals(name.getNstat())) {
				foundPreferredName = name;
				break;
			}
		}
		return foundPreferredName;
	}

	public Boolean getDeleted() {
		return this.deleted;
	}

	public void setDeleted(final Boolean deleted) {
		this.deleted = deleted;
	}

	public String getMethodNumber() {
		return this.methodNumber;
	}

	public void setMethodNumber(final String methodNumber) {
		this.methodNumber = methodNumber;
	}

	public String getMethodGroup() {
		return this.methodGroup;
	}

	public void setMethodGroup(final String methodGroup) {
		this.methodGroup = methodGroup;
	}

	public String getGermplasmPeferredName() {
		return this.germplasmPeferredName;
	}

	public void setGermplasmPeferredName(final String germplasmPeferredName) {
		this.germplasmPeferredName = germplasmPeferredName;
	}

	public String getFemaleParentPreferredName() {
		return this.femaleParentPreferredName;
	}

	public void setFemaleParentPreferredName(final String femaleParentPreferredName) {
		this.femaleParentPreferredName = femaleParentPreferredName;
	}

	public String getFemaleParentPreferredID() {
		return this.femaleParentPreferredID;
	}

	public void setFemaleParentPreferredID(final String femaleParentPreferredID) {
		this.femaleParentPreferredID = femaleParentPreferredID;
	}

	public String getMaleParentPreferredName() {
		return this.maleParentPreferredName;
	}

	public void setMaleParentPreferredName(final String maleParentPreferredName) {
		this.maleParentPreferredName = maleParentPreferredName;
	}

	public String getMaleParentPreferredID() {
		return this.maleParentPreferredID;
	}

	public void setMaleParentPreferredID(final String maleParentPreferredID) {
		this.maleParentPreferredID = maleParentPreferredID;
	}

	public String getMethodCode() {
		return this.methodCode;
	}

	public void setMethodCode(final String methodCode) {
		this.methodCode = methodCode;
	}

	public String getGermplasmPeferredId() {
		return this.germplasmPeferredId;
	}

	public void setGermplasmPeferredId(final String germplasmPeferredId) {
		this.germplasmPeferredId = germplasmPeferredId;
	}

	public String getGermplasmNamesString() {
		return this.germplasmNamesString;
	}

	public void setGermplasmNamesString(final String germplasmNamesString) {
		this.germplasmNamesString = germplasmNamesString;
	}

	public String getGermplasmDate() {
		return this.germplasmDate;
	}

	public void setGermplasmDate(final String germplasmDate) {
		this.germplasmDate = germplasmDate;
	}

	public Map<String, String> getAttributeTypesValueMap() {
		return ImmutableMap.copyOf(this.attributeTypesValueMap);
	}

	public void setAttributeTypesValueMap(final Map<String, String> attributeTypesValueMap) {
		if (attributeTypesValueMap == null) {
			throw new NullArgumentException("attributeTypesValueMap must not be null");
		}
		this.attributeTypesValueMap = attributeTypesValueMap;
	}

	public Map<String, String> getNameTypesValueMap() {
		return ImmutableMap.copyOf(this.nameTypesValueMap);
	}

	public void setNameTypesValueMap(final Map<String, String> nameTypesValueMap) {
		if (this.attributeTypesValueMap == null) {
			throw new NullArgumentException("nameTypesValueMap must not be null");
		}
		this.nameTypesValueMap = nameTypesValueMap;
	}

	public String getGroupSourcePreferredName() {
		return this.groupSourcePreferredName;
	}

	public void setGroupSourcePreferredName(final String groupSourcePreferredName) {
		this.groupSourcePreferredName = groupSourcePreferredName;
	}

	public String getGroupSourceGID() {
		return this.groupSourceGID;
	}

	public void setGroupSourceGID(final String groupSourceGID) {
		this.groupSourceGID = groupSourceGID;
	}

	public String getImmediateSourcePreferredName() {
		return this.immediateSourcePreferredName;
	}

	public void setImmediateSourcePreferredName(final String immediateSourcePreferredName) {
		this.immediateSourcePreferredName = immediateSourcePreferredName;
	}

	public String getImmediateSourceGID() {
		return this.immediateSourceGID;
	}

	public void setImmediateSourceGID(final String immediateSourceGID) {
		this.immediateSourceGID = immediateSourceGID;
	}

	public String getGermplasmUUID() {
		return this.germplasmUUID;
	}

	public void setGermplasmUUID(final String germplasmUUID) {
		this.germplasmUUID = germplasmUUID;
	}

	public Bibref getBibref() {
		return this.bibref;
	}

	public void setBibref(final Bibref bibref) {
		this.bibref = bibref;
	}

	public List<Progenitor> getOtherProgenitors() {
		return otherProgenitors;
	}

	public void setOtherProgenitors(final List<Progenitor> otherProgenitors) {
		this.otherProgenitors = otherProgenitors;
	}

	/**
	 * @param gids
	 * @return True if all gids are equals to the ones in otherProgenitors list in any order
	 */
	public boolean otherProgenitorsGidsEquals(final List<Integer> gids) {
		List<Integer> sortedExistingGids = this.otherProgenitors.stream().map(Progenitor::getProgenitorGid).collect(Collectors.toList());
		Collections.sort(sortedExistingGids);

		if (sortedExistingGids.isEmpty() && gids == null) {
			return true;
		}

		if ((sortedExistingGids == null && gids != null)
			|| sortedExistingGids != null && gids == null
			|| sortedExistingGids.size() != gids.size()) {
			return false;
		}

		final List<Integer> sortedGids = new ArrayList<>(gids);
		Collections.sort(sortedGids);

		return sortedExistingGids.equals(sortedGids);
	}

	public boolean isTerminalAncestor() {
		return new Integer(0).equals(this.gpid1) && new Integer(0).equals(gpid2);
	}

	public Optional<Progenitor> findByProgNo(final Integer progNo) {
		return this.otherProgenitors.stream().filter(p -> progNo.equals(p.getProgenitorNumber())).findFirst();
	}

	@Override
	public Germplasm clone() {
		Germplasm germplasm;
		try {
			germplasm = (Germplasm) super.clone();
		} catch (final CloneNotSupportedException e) {
			germplasm = new Germplasm(this.gid, this.methodId, this.gnpgs, this.gpid1, this.gpid2,
				this.userId, this.lgid, this.locationId, this.gdate, this.preferredName);
			germplasm.setMethod((Method) this.method.clone());
			//TODO Complete with other objects
		}
		return germplasm;
	}
}
